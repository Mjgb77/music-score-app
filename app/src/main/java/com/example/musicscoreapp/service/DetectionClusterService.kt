package com.example.musicscoreapp.service

import android.app.Activity
import android.graphics.Bitmap
import com.example.musicscoreapp.model.Recognition
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class DetectionClusterService(
        context: Activity,
        anchors: IntArray,
        masks: Array<IntArray>,
        modelFilename: String,
        gpuModelFilename: String?,
        labelFilename: String,
        confidenceThreshold: Float = 0.8F,
        numThreads: Int = 1
): CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private val cpuDetectionService = DetectionService(context, anchors, masks, modelFilename, labelFilename, DetectionService.Engine.CPU, confidenceThreshold, numThreads)
    private val gpuDetectionService =
            if (gpuModelFilename != null)
                DetectionService(context, anchors, masks, gpuModelFilename, labelFilename, DetectionService.Engine.GPU, confidenceThreshold, 1)
            else
                null

    suspend fun initializeCPU() {
        cpuDetectionService.initialize()
    }
    suspend fun initializeGPU() {
        gpuDetectionService?.initialize()
    }

    suspend fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        return withContext(coroutineContext) {
            if (gpuDetectionService?.isInitialized() == true) {
                gpuDetectionService.recognizeImage(bitmap)
            } else {
                cpuDetectionService.recognizeImage(bitmap)
            }
        }
    }
}
