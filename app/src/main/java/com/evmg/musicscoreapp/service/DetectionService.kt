package com.evmg.musicscoreapp.service

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import android.util.Size
import com.evmg.musicscoreapp.model.Recognition
import com.evmg.musicscoreapp.utils.ImageUtils
import kotlinx.coroutines.*
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import kotlin.jvm.Throws
import kotlin.math.pow
import kotlin.system.measureTimeMillis

open class DetectionService(
    private val anchors: IntArray,
    private val masks: Array<IntArray>,
    private val modelFilename: String,
    private val labelFilename: String,
    private val engine: Engine = Engine.CPU,
    private val confidenceThreshold: Float = 0.8F,
    private val numThreads: Int = 1
) {
    enum class Engine { CPU, GPU, NNAPI }
    private enum class InitializeStage { NOT_STARTED, IN_PROCESS, DONE }

    private val TAG = DetectionService::class.simpleName

    private val inputSize: Size = modelFilename.split("-")[1].split("x").let { Size(it[1].toInt(), it[0].toInt()) }
    private val isQuantized = modelFilename.contains("int8")
    private val numBytesPerChannel = if (isQuantized) 1 else 4
    private val outputWidth: Array<Size> = listOf(8, 16, 32).map { Size(inputSize.width / it, inputSize.height / it) }.toTypedArray()

    private var initializeStage = InitializeStage.NOT_STARTED

    // Float model
    private val IMAGE_MEAN = 0f

    private val IMAGE_STD = 255.0f


    private var XYSCALE = floatArrayOf(1.2f, 1.1f, 1.05f)

    private var NUM_BOXES_PER_BLOCK = 3

    /** holds a gpu delegate  */
    private var gpuDelegate: GpuDelegate? = null

    /** holds an nnapi delegate  */
    private var nnapiDelegate: NnApiDelegate? = null

    /** The loaded TensorFlow Lite model.  */
    private var tfLiteModel: ByteBuffer = ByteBuffer.allocateDirect(0)

    /** Options for configuring the Interpreter.  */
    private var tfliteOptions: Interpreter.Options = Interpreter.Options()

    // Config values.

    // Config values.
    // Pre-allocated buffers.
    private var labels: Array<String> = arrayOf()
    private var tfLite: Interpreter? = null

    private var inp_scale = 0f
    private var inp_zero_point = 0
    private var oup_scales: FloatArray = floatArrayOf()
    private var oup_zero_points: IntArray = intArrayOf()

    suspend fun initialize(context: Context) {
        withContext(Dispatchers.IO) {
            if (initializeStage == InitializeStage.NOT_STARTED) {
                initializeStage = InitializeStage.IN_PROCESS
                labels = BufferedReader(InputStreamReader(context.assets.open(labelFilename))).use {
                    it.lineSequence().toList().filter { s -> "" != s }.toTypedArray()
                }
                try {
                    val options: Interpreter.Options = Interpreter.Options()
                    options.setNumThreads(numThreads)
                    if (engine == Engine.NNAPI) {
                        nnapiDelegate = null
                        // Initialize interpreter with NNAPI delegate for Android Pie or above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            nnapiDelegate = NnApiDelegate()
                            options.addDelegate(nnapiDelegate)
                            options.setNumThreads(numThreads)
                            //                    options.setUseNNAPI(false);
//                    options.setAllowFp16PrecisionForFp32(true);
//                    options.setAllowBufferHandleOutput(true);
                            options.setUseNNAPI(true)
                        }
                    }
                    if (engine == Engine.GPU) {
                        val gpu_options: GpuDelegate.Options = GpuDelegate.Options()
                        gpu_options.setPrecisionLossAllowed(true) // It seems that the default is true
                        gpu_options.setInferencePreference(GpuDelegate.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED)
                        gpu_options.setQuantizedModelsAllowed(true)
                        gpuDelegate = GpuDelegate(gpu_options)
                        options.addDelegate(gpuDelegate)
                    }
                    Log.d(TAG, "Loading TFLite: " + measureTimeMillis {
                        tfLiteModel = loadModelFile(context.assets, modelFilename)
                        tfLite = Interpreter(tfLiteModel, options)
                    } + "ms")
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
                if (isQuantized) {
                    val inpten: Tensor = tfLite!!.getInputTensor(0)
                    inp_scale = inpten.quantizationParams().getScale()
                    inp_zero_point = inpten.quantizationParams().getZeroPoint()
                    oup_scales = FloatArray(masks.size)
                    oup_zero_points = IntArray(masks.size)
                    for (i in masks.indices) {
                        val oupten: Tensor = tfLite!!.getOutputTensor(i)
                        oup_scales[i] = oupten.quantizationParams().getScale()
                        oup_zero_points[i] = oupten.quantizationParams().getZeroPoint()
                    }
                }
                initializeStage = InitializeStage.DONE
            }
        }
    }

    private fun getInputSize(): Int {
        throw NotImplementedError()
    }

    private fun enableStatLogging(logStats: Boolean) {}

    private fun getStatString(): String? {
        return ""
    }

    private fun close() {
        tfLite?.close()
        tfLite = null
        if (gpuDelegate != null) {
            gpuDelegate?.close()
            gpuDelegate = null
        }
        if (nnapiDelegate != null) {
            nnapiDelegate?.close()
            nnapiDelegate = null
        }
        tfLiteModel = ByteBuffer.allocateDirect(0)
    }

    private fun setNumThreads(num_threads: Int) {
        if (tfLite != null) tfLite?.setNumThreads(num_threads)
    }

    private fun setUseNNAPI(isChecked: Boolean) {
        if (tfLite != null) tfLite?.setUseNNAPI(isChecked)
    }

    private fun recreateInterpreter() {
        if (tfLite != null) {
            tfLite?.close()
            tfLite = Interpreter(tfLiteModel, tfliteOptions)
        }
    }

    private fun useGpu() {
        if (gpuDelegate == null) {
            gpuDelegate = GpuDelegate()
            tfliteOptions.addDelegate(gpuDelegate)
            recreateInterpreter()
        }
    }

    private fun useCPU() {
        recreateInterpreter()
    }

    private fun useNNAPI() {
        nnapiDelegate = NnApiDelegate()
        tfliteOptions.addDelegate(nnapiDelegate)
        recreateInterpreter()
    }

    //non maximum suppression
    private fun nms(list: ArrayList<Recognition>): ArrayList<Recognition> {
        val nmsList = ArrayList<Recognition>()
        for (k in labels.indices) {
            //1.find max confidence per class
            val pq: PriorityQueue<Recognition> = PriorityQueue(50) { lhs, rhs ->
                // Intentionally reversed to put high confidence at the head of the queue.
                rhs.confidence.compareTo(lhs.confidence)
            }
            for (i in 0 until list.size) {
                if (list[i].detectedClass == k) {
                    pq.add(list[i])
                }
            }

            //2.do non maximum suppression
            while (pq.size > 0) {
                //insert detection with max confidence
                val detections: Array<Recognition> = pq.toArray(arrayOfNulls(pq.size))
                val max: Recognition = detections[0]
                nmsList.add(max)
                pq.clear()
                for (j in 1 until detections.size) {
                    val detection: Recognition = detections[j]
                    val b: RectF = detection.location
                    if (boxIntersectionOverUnionRation(max.location, b) < mNmsThresh) {
                        pq.add(detection)
                    }
                }
            }
        }
        return nmsList
    }

    private val mNmsThresh = 0.3f

    private fun boxIntersectionOverUnionRation(a: RectF, b: RectF): Float {
        return boxIntersection(a, b) / boxUnion(a, b)
    }

    private fun boxIntersection(a: RectF, b: RectF): Float {
        val w = overlap(
            (a.left + a.right) / 2, a.right - a.left,
            (b.left + b.right) / 2, b.right - b.left
        )
        val h = overlap(
            (a.top + a.bottom) / 2, a.bottom - a.top,
            (b.top + b.bottom) / 2, b.bottom - b.top
        )
        if (w < 0 || h < 0) return 0F
        return w * h
    }

    private fun boxUnion(a: RectF, b: RectF): Float {
        val i = boxIntersection(a, b)
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
    }

    private fun overlap(x1: Float, w1: Float, x2: Float, w2: Float): Float {
        val l1 = x1 - w1 / 2
        val l2 = x2 - w2 / 2
        val left = if (l1 > l2) l1 else l2
        val r1 = x1 + w1 / 2
        val r2 = x2 + w2 / 2
        val right = if (r1 < r2) r1 else r2
        return right - left
    }

    protected val BATCH_SIZE = 1
    protected val PIXEL_SIZE = 3

    fun isInitialized(): Boolean {
        return initializeStage == InitializeStage.DONE
    }

    suspend fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        if (initializeStage != InitializeStage.DONE) {
            return listOf()
        }
        return withContext(Dispatchers.IO) {
            val adjusted = ImageUtils.adjustImageToSize(bitmap, inputSize)
            recognizeImageBlocking(adjusted.bmp)
                .map { r ->
                    Recognition(r.id, r.title, r.confidence, adjusted.reverser.invoke(r.location), r.detectedClass)
                }
                .map { postProcess(it, bitmap) }
        }
    }
    private fun recognizeImageBlocking(img: Bitmap): List<Recognition> {
        val bitmap = img
        //
        // 1. Prepare TFLite Input
        //
        val outputMap: MutableMap<Int, Any> = HashMap()
        val imgData = ByteBuffer.allocateDirect(1 * inputSize.width * inputSize.height * 3 * numBytesPerChannel)
        imgData.order(ByteOrder.nativeOrder())
        Log.d(TAG, "Prepare Input and Output: " + measureTimeMillis {

            val intValues = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            imgData.rewind()
            for (i in 0 until inputSize.height) {
                for (j in 0 until inputSize.width) {
                    val pixelValue = intValues[i * inputSize.width + j]
                    if (isQuantized) {
                        // Quantized model
                        imgData.put(((((pixelValue shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toByte())
                        imgData.put(((((pixelValue shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toByte())
                        imgData.put((((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toByte())
                    } else { // Float model
                        imgData.putFloat((((pixelValue shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                        imgData.putFloat((((pixelValue shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                        imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    }
                }
            }

            // Create outputArrays
            val outData = {
                val outData = arrayListOf<ByteBuffer>()
                val shape: IntArray = tfLite!!.getOutputTensor(0).shape()
                val numClass = shape[shape.size - 1]
                for (i in masks.indices) {
                    outData.add(
                        ByteBuffer.allocateDirect(
                            1
                                    * outputWidth.get(i).width
                                    * outputWidth.get(i).height
                                    * masks[i].size
                                    * (5 + numClass)
                                    * numBytesPerChannel
                        )
                    )
                    outData.get(i).order(ByteOrder.nativeOrder())
                }
                outData.toTypedArray()
            }.invoke()

            for (i in outputWidth.indices) {
                outData[i].rewind()
                outputMap[i] = outData.get(i)
            }

        }.toString() + "ms")
        val inputArray = arrayOf<Any?>(imgData)

        //
        // 2. Run TFLite detection
        //
        Log.d(TAG, "Running TFLite Detection: " + measureTimeMillis {

            tfLite?.runForMultipleInputsOutputs(inputArray, outputMap)

        }.toString() + "ms")

        //
        // 3. Parse detections into ArrayList<Recognition>
        //
        val detections: ArrayList<Recognition> = ArrayList()
        var total = 0L
        for (i in outputWidth.indices) {
            total += measureTimeMillis {
                val gridSize = outputWidth[i]
                val byteBuffer: ByteBuffer = outputMap[i] as ByteBuffer
                byteBuffer.rewind()
                val out = Array(1) {
                    Array(NUM_BOXES_PER_BLOCK) {
                        Array(gridSize.width * gridSize.height) {
                            FloatArray(5 + labels.size)
                        }
                    }
                }
                if (isQuantized) {
                    for (b in 0 until NUM_BOXES_PER_BLOCK) {
                        for (y in 0 until gridSize.height) {
                            for (x in 0 until gridSize.width) {
                                for (c in 0 until 5 + labels.size) {
                                    out[0][b][y * gridSize.width + x][c] =
                                        oup_scales[i] * ((byteBuffer.get()
                                            .toInt() and 0xFF) - oup_zero_points[i])
                                }
                            }
                        }
                    }
                } else {
                    for (b in 0 until NUM_BOXES_PER_BLOCK) {
                        for (y in 0 until gridSize.height) {
                            for (x in 0 until gridSize.width) {
                                for (c in 0 until 5 + labels.size) {
                                    out[0][b][y * gridSize.width + x][c] = byteBuffer.float
                                }
                            }
                        }
                    }

                }

                for (y in 0 until gridSize.height) {
                    for (x in 0 until gridSize.width) {
                        for (b in 0 until NUM_BOXES_PER_BLOCK) {
                            val offset: Int =
                                ((gridSize.width * (NUM_BOXES_PER_BLOCK * (labels.size + 5))) * y
                                        ) + ((NUM_BOXES_PER_BLOCK * (labels.size + 5)) * x
                                        ) + ((labels.size + 5) * b)
                            val confidence: Float = expit(out[0][b][y * gridSize.width + x][4])
                            var detectedClassIndex = -1
                            var maxClass = 0f
                            val classes = FloatArray(labels.size)
                            for (c in labels.indices) {
                                classes[c] = expit(out[0][b][y * gridSize.width + x][5 + c])
                            }
                            for (c in labels.indices) {
                                if (classes[c] > maxClass) {
                                    detectedClassIndex = c
                                    maxClass = classes[c]
                                }
                            }
                            val confidenceInClass = maxClass * confidence
                            if (confidenceInClass > confidenceThreshold) {
                                val xPos: Float =
                                    (x + expit(out[0][b][y * gridSize.width + x][0]) * 2f - 0.5f) * (1.0f * inputSize.width / gridSize.width)
                                val yPos: Float =
                                    (y + expit(out[0][b][y * gridSize.width + x][1]) * 2f - 0.5f) * (1.0f * inputSize.height / gridSize.height)
                                val w =
                                    ((expit(out[0][b][y * gridSize.width + x][2]).toDouble() * 2).pow(
                                        2.0
                                    ) * anchors[2 * masks[i][b]]).toFloat()
                                val h =
                                    (Math.pow(
                                        expit(out[0][b][y * gridSize.width + x][3]).toDouble() * 2,
                                        2.0
                                    ) * anchors[2 * masks[i][b] + 1]).toFloat()
                                val rect = RectF(
                                    Math.max(0f, xPos - w / 2),
                                    Math.max(0f, yPos - h / 2),
                                    Math.min(bitmap.width - 1.toFloat(), xPos + w / 2),
                                    Math.min(bitmap.height - 1.toFloat(), yPos + h / 2)
                                )
                                detections.add(
                                    Recognition(
                                        "" + offset, labels[detectedClassIndex],
                                        confidenceInClass, rect, detectedClassIndex
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "Decoding detections: ${total}ms")

        return nms(detections)
    }

    fun postProcess(recognition: Recognition, bitmap: Bitmap): Recognition {
        return when (recognition.title) {
            "barline" -> ObjectAdjustments.adjustBarline(recognition, bitmap)
            "Staff" -> ObjectAdjustments.adjustStaff(recognition, bitmap)
            else -> recognition
        }
    }

    private fun expit(x: Float) : Float {
        return (1.0 / (1.0 + Math.exp(-x.toDouble()))).toFloat()
    }

    @Throws(IOException::class)
    private fun loadModelFile(assets: AssetManager, modelFilename: String?): ByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

}
