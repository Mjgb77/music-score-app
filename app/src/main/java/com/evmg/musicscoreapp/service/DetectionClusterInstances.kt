package com.evmg.musicscoreapp.service

import android.content.Context
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.math.roundToInt

object DetectionClusterInstances {

    val figureDetectionService = DetectionClusterService(
//        doubleArrayOf(351.25000,  35.90625, 336.75000,  40.81250, 336.25000,  43.53125, 338.50000,  45.62500, 386.50000,  40.06250, 337.25000,  49.18750, 395.50000,  43.00000, 395.75000,  45.18750, 383.00000,  49.71875).map { it.roundToInt() }.toIntArray(),
        intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
        arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
        modelFilename = "figures-192x1280-s2270e-b6-int8.tflite",
        gpuModelFilename = "figures-192x1280-s2270e-b6-fp16.tflite",
        labelFilename = "classes.txt",
        numThreads = 4,
        confidenceThreshold = 0.5F
    )

    val staffDetectionService = DetectionClusterService(
        doubleArrayOf(351.25000,  35.90625, 336.75000,  40.81250, 336.25000,  43.53125, 338.50000,  45.62500, 386.50000,  40.06250, 337.25000,  49.18750, 395.50000,  43.00000, 395.75000,  45.18750, 383.00000,  49.71875).map { it.roundToInt() }.toIntArray(),
//        intArrayOf(10, 13, 16, 30, 33, 23, 30, 61, 62, 45, 59, 119, 116, 90, 156, 198, 373, 326),
        arrayOf(intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8)),
        modelFilename = "staff-544x384-s1000e-32b-int8.tflite",
        gpuModelFilename = "staff-544x384-s1000e-32b-fp16.tflite",
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
