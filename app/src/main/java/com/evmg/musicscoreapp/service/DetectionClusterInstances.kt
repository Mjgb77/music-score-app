package com.evmg.musicscoreapp.service

import android.content.Context
import kotlinx.coroutines.*
import java.lang.Exception

object DetectionClusterInstances {

    val figureDetectionService = DetectionClusterService(
        intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
        arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
        modelFilename = "figures-192x1280-s2270e-b6-int8.tflite",
        gpuModelFilename = "figures-192x1280-s2270e-b6-int8.tflite",
        labelFilename = "classes.txt",
        numThreads = 4,
        confidenceThreshold = 0.5F
    )

    val staffDetectionService = DetectionClusterService(
        intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
        arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
        modelFilename = "staff-416x320-e1800-b32-int8.tflite",
        gpuModelFilename = "staff-416x320-e1800-b32-int8.tflite",
        labelFilename = "staff-classes.txt",
        confidenceThreshold = 0.3F,
        numThreads = 4
    )

    lateinit var scope: CoroutineScope

    fun init(context: Context) {
        scope = CoroutineScope(Job() + Dispatchers.Default)
        GlobalScope.launch {
            staffDetectionService.initializeCPU(context)
            figureDetectionService.initializeCPU(context)

//            try {
//                staffDetectionService.initializeGPU(context)
//                figureDetectionService.initializeGPU(context)
//            } catch(e: Exception) {}
        }
    }
}
