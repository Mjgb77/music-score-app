package com.example.musicscoreapp

import android.app.Activity
import android.graphics.Color
import android.graphics.RectF
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import com.example.musicscoreapp.model.StaffRecognition
import com.example.musicscoreapp.objectparsing.StaffToMidi
import com.example.musicscoreapp.service.DetectionClusterService
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

    private val figureDetectionService = DetectionClusterService(
            context,
            intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
            arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
            modelFilename = "figures-192x1280-s2270e-b6-int8.tflite",
            gpuModelFilename = "figures-192x1280-s2270e-b6-int8.tflite",
            labelFilename = "classes.txt",
            numThreads = 4,
            confidenceThreshold = 0.5F)

    private val staffDetectionService = DetectionClusterService(
            context,
            intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
            arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
            modelFilename = "staff-416x320-e1800-b32-int8.tflite",
            gpuModelFilename = "staff-416x320-e1800-b32-int8.tflite",
            labelFilename = "staff-classes.txt",
            confidenceThreshold = 0.3F,
            numThreads = 4)

    fun readMidi(images : Array<String>, callback: (String) -> Unit) {
        val job = launch {
            staffDetectionService.initializeCPU()
            figureDetectionService.initializeCPU()
        }
        launch {
            staffDetectionService.initializeGPU()
            figureDetectionService.initializeGPU()
        }

        job.invokeOnCompletion {
            launch {
                val image = pictureService.readBitmap { File(images[0]).inputStream() }
                Log.d("MY-DEB", "$PREFIX width:${image.width} height:${image.height}")
                val staffs = staffDetectionService.recognizeImage(image)
                val recognitions = ArrayList<StaffRecognition>()
                val absRecognition = ArrayList<RectF>()
                for (staff in staffs) {
                    val staffImage = ImageUtils.crop(image, ImageUtils.snap(staff.location))
                    val recognitions2 = figureDetectionService.recognizeImage(staffImage.bmp)
                    recognitions.add(StaffRecognition(staff, recognitions2))
                    absRecognition.addAll(recognitions2.map { staffImage.reverser(it.location) })
                }

                val mutable = image.copy(image.config, true)
                for (a in absRecognition) {
                    ImageUtils.paintRect(mutable, a, Color.RED)
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
                callback?.invoke(file.path)
            }
        }
    }
}