package com.evmg.musicscoreapp.service

import android.R.attr.bitmap
import android.content.Context
import android.graphics.*
import android.util.Log
import com.evmg.musicscoreapp.model.Recognition
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.model.StaffRecognition
import com.evmg.musicscoreapp.objectparsing.StaffToMidi
import com.evmg.musicscoreapp.utils.ImageUtils
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.ArrayBlockingQueue


object ScoreToMidiConverter {
    lateinit var scope: CoroutineScope
    private var curSheetID = 0
    private val PREFIX = "-".repeat(20)
    private val processingSheets = hashMapOf<Int, Deferred<ArrayList<StaffRecognition>>>()
    private val processor = ArrayBlockingQueue<Deferred<ArrayList<StaffRecognition>>>(100)

    private val COLORS = arrayOf(
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

    private fun init() {
        scope = CoroutineScope(Job() + Dispatchers.Default)
        GlobalScope.launch(context = Dispatchers.IO) {
            while (true) {
                if (processor.isEmpty()) delay(100)
                else processor.poll()?.await()
            }
        }
    }

    fun processSheet(file: File, context: Context):Int {
        if (!(::scope.isInitialized)) init()

        processingSheets[curSheetID] = scope.async(start = CoroutineStart.LAZY) {
            val image = PictureService.readBitmap(file)
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
            val mutable = image.copy(image.config, true)
            for (a in recognitions) {
                ImageUtils.paintRect(mutable, a.staff.location, Color.DKGRAY, 5)
                for(b in a.objects) {
                    ImageUtils.paintRect(
                        mutable,
                        b.location,
                        COLORS[b.detectedClass % COLORS.size],
                        3
                    )
                }
            }

            val predictions = File(context.getExternalFilesDir("Scores"), "predicitions.jpg")
            predictions.createNewFile()
            val os: OutputStream = BufferedOutputStream(FileOutputStream(predictions))
            mutable.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.close()

            recognitions
        }
        processor.add(processingSheets[curSheetID])

        return curSheetID++
    }

    fun removeSheet(sheetID: Int) {
        processingSheets[sheetID]!!.cancel()
    }

    fun saveScore(score: Score, context: Context) {
        if (score.sheets == null) return
        scope.launch {
            try {
                val deferredSheets = score.sheets.map { processingSheets[it.id]!! }
                val allStaffs = deferredSheets.map { it.await() }.flatten()
                val midiBytes =
                    StaffToMidi.staffToMidi(allStaffs, score.tempo, score.instrument.toByte())
                val fileStorageHelper = ScoreDb(context)
                fileStorageHelper.saveScore(score, midiBytes)
            } catch (ex: Exception) {
                Log.e(PREFIX, "Error creating staff", ex)
            }
        }
    }
}
