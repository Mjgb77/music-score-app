package com.example.musicscoreapp

import android.app.Activity
import android.graphics.*
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.example.musicscoreapp.model.Recognition
import com.example.musicscoreapp.model.StaffRecognition
import com.example.musicscoreapp.objectparsing.StaffToMidi
import com.example.musicscoreapp.service.DetectionClusterInstances
import com.example.musicscoreapp.service.DetectionClusterInstances.figureDetectionService
import com.example.musicscoreapp.service.DetectionClusterInstances.staffDetectionService
import com.example.musicscoreapp.service.DetectionClusterService
import com.example.musicscoreapp.service.ObjectAdjustments
import com.example.musicscoreapp.service.PictureService
import com.example.musicscoreapp.utils.ImageUtils
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class DetectionHelper(private val context: Activity) : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    private val PREFIX = "-".repeat(20)
    private val pictureService = PictureService(context)

    private val COLORS = arrayOf (
        Color.parseColor("#3d3df5"),
        Color.parseColor("#b25050"),
        Color.parseColor("#3df53d"),
        Color.parseColor("#f59331"),
        Color.parseColor("#000000"),
        Color.parseColor("#00f8ff"),
        Color.parseColor("#280a4c"),
        Color.parseColor("#5986b3"),
        Color.parseColor("#b83df5"),
        Color.parseColor("#aaf0d1"),
        Color.parseColor("#2181b1"),
        Color.parseColor("#198139"),
        Color.parseColor("#fa3253"),
        Color.parseColor("#ff00cc"),
        Color.parseColor("#83e070"),
        Color.parseColor("#ddff33"),
        Color.parseColor("#7cff7c"),
        Color.parseColor("#fa7dbb"),
        Color.parseColor("#28c6f1"),
        Color.parseColor("#000000"),
        Color.parseColor("#34d1b7")
    )


    fun readMidi(images : Array<String>, callback: (String) -> Unit) {
        launch {
            DetectionClusterInstances.init(context)
            val image = pictureService.readBitmap { File(images[0]).inputStream() }
//            ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f)})

            val paint = Paint().apply {
                this.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f)})
            }

            Canvas(image).drawBitmap(image, 0F, 0F, paint)

            Log.d("MY-DEB", "$PREFIX width:${image.width} height:${image.height}")
            val staffs = staffDetectionService.recognizeImage(image)
            val recognitions = ArrayList<StaffRecognition>()

            for (staff in staffs) {
                val staffImage = ImageUtils.crop(image, ImageUtils.snap(staff.location))
                val recognitions2 = figureDetectionService.recognizeImage(staffImage.bmp)
                recognitions.add(StaffRecognition(staff, recognitions2.map {r ->
                    Recognition(r.id, r.title, r.confidence, staffImage.reverser(r.location), r.detectedClass)
                }))
            }

            val mutable = image.copy(image.config, true)
            for (a in recognitions) {
                ImageUtils.paintRect(mutable, a.staff.location, Color.YELLOW, 3)
                for(b in a.objects) {
                    ImageUtils.paintRect(mutable, b.location, COLORS[b.detectedClass%COLORS.size], 3)
                }
            }
            withContext(Dispatchers.Main) {
                //TODO do something with this imagePreview
            }

            val bytesToWrite = StaffToMidi.staffToMidi(recognitions.sortedBy { it.staff.location.top })

            val createFile = {
                // Create an image file name
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                File.createTempFile(
                        "MIDI_${timeStamp}_", /* prefix */
                        ".mid", /* suffix */
                        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) /* directory */
                )
            }

            val file = createFile()
            file.writeBytes(bytesToWrite)
            callback.invoke(file.path)
        }
    }
}