package com.example.musicscoreapp.service

import android.content.Context
import android.graphics.*
import android.util.Log
import com.example.musicscoreapp.FileStorageHelper
import com.example.musicscoreapp.model.Recognition
import com.example.musicscoreapp.model.StaffRecognition
import com.example.musicscoreapp.objectparsing.StaffToMidi
import com.example.musicscoreapp.utils.ImageUtils
import com.example.musicscoreapp.utils.Score
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

object ScoreToMidiConverter {
    lateinit var scope:CoroutineScope
    private var curSheetID = 0
    private val PREFIX = "-".repeat(20)
    private val processingSheets = hashMapOf<Int, Deferred<ArrayList<StaffRecognition>>>()
    private val processor = ArrayBlockingQueue<Deferred<ArrayList<StaffRecognition>>>(100)

    private fun init() {
        scope = CoroutineScope(Job() + Dispatchers.Default)
        scope.launch {
            while (true) {
                processor.poll()?.join()
            }
        }
    }

    fun processSheet(image: Bitmap, context: Context):Int {
        if (!(::scope.isInitialized)) init()

        processingSheets[curSheetID] = scope.async(start = CoroutineStart.LAZY) {
            Log.d("MY-DEB", "$PREFIX width:${image.width} height:${image.height} -> started")
            DetectionClusterInstances.init(context)
            val staffs = DetectionClusterInstances.staffDetectionService
                .recognizeImage(image)
                .sortedBy { it.location.top }

            val recognitions = ArrayList<StaffRecognition>()

            for (staff in staffs) {
                val staffImage = ImageUtils.crop(image, ImageUtils.snap(staff.location))
                val recognitions2 =
                    DetectionClusterInstances.figureDetectionService.recognizeImage(staffImage.bmp)
                recognitions.add(StaffRecognition(staff, recognitions2.map { r ->
                    Recognition(
                        r.id,
                        r.title,
                        r.confidence,
                        staffImage.reverser(r.location),
                        r.detectedClass
                    )
                }))
            }
            Log.d("MY-DEB", "$PREFIX width:${image.width} height:${image.height} -> ended")
            recognitions
        }
        processor.add(processingSheets[curSheetID])

        return curSheetID++
    }

    fun removeSheet(sheetID: Int) {
        processingSheets[sheetID]!!.cancel()
    }

    fun saveScore(score: Score, context: Context) {
        scope.launch {
            try {
                val deferredSheets = score.sheets.map { processingSheets[it.id]!! }
                val allStaffs = deferredSheets.map { it.await() }.flatten()
                val midiBytes =
                    StaffToMidi.staffToMidi(allStaffs, score.tempo, score.instrument.toByte())
                val fileStorageHelper = FileStorageHelper(context)
                fileStorageHelper.addScore(score, midiBytes)
            } catch (ex: Exception) {
                Log.e(PREFIX, "Error creating staff", ex)
            }
        }

    }
}
