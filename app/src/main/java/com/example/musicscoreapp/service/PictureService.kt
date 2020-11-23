package com.example.musicscoreapp.service

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.musicscoreapp.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class PictureService(private val context: Activity) : ViewModel() {

    // Request codes
    private val REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1242
    private val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    private val READ_EXTERNAL_STORAGE_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private val unprocessedRequests = HashMap<Int, TakePictureAction>()


    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    fun takePicture(callback: (File) -> Unit) {
        val permissions = arrayOf(
                CAMERA_PERMISSION,
                READ_EXTERNAL_STORAGE_PERMISSION)
        val requestId = Random.nextInt(60000)
        if (!hasPermissions(context, *permissions)) {
            ActivityCompat.requestPermissions(context, permissions, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)
        } else {
            val file = createImageFile()
            try {
                val pickImage = Intent(Intent.ACTION_GET_CONTENT).also {
                    it.type = "image/*"
                }
                val photoURI: Uri = FileProvider.getUriForFile(
                    context,
                    "com.example.musicscoreapp.fileprovider",
                    file)
                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }

                val chooser = Intent.createChooser(pickImage, "Select an Image").also {
                    it.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(captureImage))
                }

                unprocessedRequests[requestId] = TakePictureAction(callback, file)
                context.startActivityForResult(chooser, requestId)
            } catch (e: ActivityNotFoundException) {
            }
        }
    }

    suspend fun readBitmap(image: File): Bitmap {
        return readBitmap { FileInputStream(image) }
    }

    suspend fun readBitmap(streamProvider: () -> InputStream): Bitmap {
        // Move the execution of the coroutine to the I/O dispatcher
        return withContext(Dispatchers.IO) {
            val orientation = streamProvider.invoke().use { inputStream ->
                val ei = ExifInterface(inputStream)
                ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
            val img = streamProvider.invoke().use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
            val rotatedImage = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> ImageUtils.rotateImage(img, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> ImageUtils.rotateImage(img, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> ImageUtils.rotateImage(img, 270)
                else -> img
            }
            rotatedImage
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (unprocessedRequests.containsKey(requestCode)) {
            val action = unprocessedRequests.remove(requestCode)
            if (resultCode == AppCompatActivity.RESULT_OK && action != null) {
                GlobalScope.launch(Dispatchers.IO) {
                    if (data?.data != null) {
                        context.contentResolver.openInputStream(data.data!!).use {
                            val bitmapData = BitmapFactory.decodeStream(it)
                            FileOutputStream(action.path).use {
                                bitmapData.compress(Bitmap.CompressFormat.JPEG, 100, it)
                            }
                        }
                    }
                    action.callback.invoke(action.path)
                }
            }

        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "JPEG_${timeStamp}.jpg"
        )
    }

    fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {

    }

    class TakePictureAction(val callback: (File) -> Unit, val path: File)
}
