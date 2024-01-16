package com.surendramaran.yolov8tflite

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Detector(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String,
    private val detectorListener: DetectorListener,
    var confthreshold: Float = 0.5f,
    var iouThreshold: Float = 0.5F,
    var numThreadsUsed: Int = 4,
    var maxResults: Int = 10,
    var utilizeGPU: Boolean = false,
) {

    private var interpreter: InterpreterApi? = null
    private var labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    fun setup() {
        try {
            val compatList = CompatibilityList()
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()

            options.apply{
                this.runtime = InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY
                if(utilizeGPU && compatList.isDelegateSupportedOnThisDevice){
                    val delegateOptions = compatList.bestOptionsForThisDevice
                    this.addDelegate(GpuDelegate(delegateOptions))
                } else {
                    this.setNumThreads(numThreadsUsed)
                }
            }
            interpreter = Interpreter(model, options)
            initializeInterpreter()
            loadLabels()

//            TfLiteGpu.isGpuDelegateAvailable(context)
//                .addOnSuccessListener { useGpu ->
//                    options.apply {
//                        runtime = InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY
//                        numThreads = numThreadsUsed
//                        if (utilizeGPU && useGpu) addDelegateFactory(GpuDelegateFactory())
//                    }
//
//                    try {
//
//
//                    } catch (e: Exception) {
//                        Log.e("InterpreterError", "Error creating TensorFlow Lite interpreter: ${e.message}")
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("GpuDelegateError", "Error checking GPU delegate availability: ${e.message}")
//                }
        } catch (e: Exception) {
            Log.e("SetupError", "Error setting up TensorFlow Lite: ${e.message}")
        }
    }
    private fun initializeInterpreter() {
        interpreter?.let {
            val inputShape = it.getInputTensor(0).shape()
            val outputShape = it.getOutputTensor(0).shape()

            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]
            numChannel = outputShape[1]
            numElements = outputShape[2]
        }
    }
    private fun loadLabels() {
        try {
            context.assets.open(labelPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (!line.isNullOrEmpty()) {
                        labels.add(line)
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("LabelLoadError", "Error loading labels: ${e.message}")
        }
    }

    fun clear() {
        interpreter = null
    }

    fun destroy() {
        interpreter?.close()
    }

    fun detect(frame: Bitmap) {
        if (interpreter == null)
        {
            this.setup()
            return
        }
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) return

        var inferenceTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1 , numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter?.run(imageBuffer, output.buffer)

        bestBox(output.floatArray)?.let { bestBoxes ->
            detectorListener.onDetect(bestBoxes, SystemClock.uptimeMillis() - inferenceTime)
            detectorListener.onCountsUpdated(bestBoxes)
        } ?: detectorListener.onEmptyDetect()
    }

    private fun bestBox(array: FloatArray): List<BoundingBox>? {
        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            val confidences = (4 until numChannel).map { array[c + numElements * it] }
            val cnf = confidences.maxOrNull() ?: continue
            if (cnf <= confthreshold) continue

            val cls = confidences.indexOf(cnf)
            val boundingBox = createBoundingBox(array, c, cls, cnf)
            if (boundingBox?.isValid() == false) continue
            if (boundingBox != null && boundingBoxes.none { calculateIoU(it, boundingBox) >= iouThreshold }) {
                boundingBoxes.add(boundingBox)
                if (boundingBoxes.size == maxResults) break
            }
        }

        return boundingBoxes.ifEmpty { null }
    }

    private fun BoundingBox.isValid(): Boolean {
        return !x1.isNaN() && !y1.isNaN() && !x2.isNaN() && !y2.isNaN() &&
                !cx.isNaN() && !cy.isNaN() && !w.isNaN() && !h.isNaN()
    }

    private fun createBoundingBox(array: FloatArray, index: Int, classIndex: Int, confidence: Float): BoundingBox? {
        val cx = array[index]
        val cy = array[index + numElements]
        val w = array[index + numElements * 2]
        val h = array[index + numElements * 3]
        val x1 = cx - w / 2
        val y1 = cy - h / 2
        val x2 = cx + w / 2
        val y2 = cy + h / 2

        if (x1 < 0f || y1 < 0f || x2 < 0f || y2 < 0f || x1 > 1f || y1 > 1f || x2 > 1f || y2 > 1f) {
            return null
        }

        return BoundingBox(x1, y1, x2, y2, cx, cy, w, h, confidence, classIndex, labels.getOrElse(classIndex) { "Unknown" })
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    interface DetectorListener {
        fun onEmptyDetect()
        fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long)
        fun onCountsUpdated(boundingBoxes: List<BoundingBox>)
    }

    companion object {
//        const val DELEGATE_CPU = 0
//        const val DELEGATE_GPU = 1
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.5F
        private const val IOU_THRESHOLD = 0.5F
    }
}