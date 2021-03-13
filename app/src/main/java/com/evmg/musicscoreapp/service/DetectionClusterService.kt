package com.evmg.musicscoreapp.service

import android.content.Context
import android.graphics.Bitmap
import com.evmg.musicscoreapp.model.Recognition
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class DetectionClusterService(
    anchors: IntArray,
    masks: Array<IntArray>,
    modelFilename: String,
    gpuModelFilename: String?,
    labelFilename: String,
    confidenceThreshold: Float = 0.8F,
    numThreads: Int = 1
) : CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    private val cpuDetectionService = DetectionService(
        anchors,
        masks,
        modelFilename,
        labelFilename,
        DetectionService.Engine.CPU,
        confidenceThreshold,
        numThreads
    )
    private val gpuDetectionService =
        if (gpuModelFilename != null)
            DetectionService(
                anchors,
                masks,
                gpuModelFilename,
                labelFilename,
                DetectionService.Engine.GPU,
                confidenceThreshold,
                1
            )
        else
            null

    suspend fun initializeCPU(context: Context) {
        cpuDetectionService.initialize(context)
    }

    suspend fun initializeGPU(context: Context) {
//        TODO: try-catch for gpu
        gpuDetectionService?.initialize(context)
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
