package com.surendramaran.yolov8tflite.fragments

import TreeDatabase
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.surendramaran.yolov8tflite.BoundingBox
import com.surendramaran.yolov8tflite.Constants
import com.surendramaran.yolov8tflite.Detector
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.databinding.FragmentCameraBinding
import com.surendramaran.yolov8tflite.entities.Tree
import com.surendramaran.yolov8tflite.model.Count
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(R.layout.fragment_camera), Detector.DetectorListener {
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var detector: Detector
    private lateinit var cameraExecutor: ExecutorService

    private val uiScope = CoroutineScope(Dispatchers.Main)

    private var counts: MutableMap<String, Count> = mutableMapOf(
        "flower" to Count("flower", 0),
        "unripe" to Count("unripe", 0),
        "underripe" to Count("underripe", 0),
        "ripe" to Count("ripe", 0),
        "abnormal" to Count("abnormal", 0)
    )
    private lateinit var countViews: MutableMap<String, TextView>
    private var totalCount: MutableMap<String, Count> = mutableMapOf(
        "flower" to Count("flower", 0),
        "unripe" to Count("unripe", 0),
        "underripe" to Count("underripe", 0),
        "ripe" to Count("ripe", 0),
        "abnormal" to Count("abnormal", 0)
    )
    private var onActivityCreatedCallback: (() -> Unit)? = null
    private lateinit var treeDao: TreeDao
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val database by lazy { TreeDatabase.getDatabase(requireContext()) }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()
        uiScope.cancel()
        detector.destroy()
        detector.clear()
        cameraExecutor.shutdown()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)

        val view = fragmentCameraBinding.countContainer

        countViews = mutableMapOf()
        val gridLayout = view.findViewById<GridLayout>(R.id.counts_grid)
        val countList = counts.entries.toList()

        for ((index, count) in countList.withIndex()) {
            val countClass = count.value.name
            val countAmount = count.value.count

            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.gravity = Gravity.CENTER_VERTICAL

            val textView = TextView(requireContext())
            val textLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = textLayoutParams
            textView.text = "${countClass}: ${countAmount}"
            textView.id = View.generateViewId()
            countViews[countClass] = textView

            val addButton = ImageView(requireContext())
            val buttonLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addButton.layoutParams = buttonLayoutParams
            addButton.setImageResource(R.drawable.baseline_add_24)
            addButton.setOnClickListener(addCountPerClassListener(count))
            addButton.id = View.generateViewId()

            linearLayout.addView(textView)
            linearLayout.addView(addButton)

            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = GridLayout.spec(index % 3)
            layoutParams.rowSpec = GridLayout.spec(index / 3)
            gridLayout.addView(linearLayout, index)
        }


        return fragmentCameraBinding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detector = Detector(requireContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, this)
        detector.setup()

        cameraExecutor = Executors.newSingleThreadExecutor()

        fragmentCameraBinding.viewFinder.post {
            setupCamera()
        }

        val countButton = view.findViewById<Button>(R.id.countBtn)
        countButton?.setOnClickListener {
            sumCounts()
        }

        val totalCountButton = view.findViewById<Button>(R.id.totalCountBtn)
        totalCountButton?.setOnClickListener {
            showTotalCountDialog()
        }

        val resetButton = view.findViewById<ImageButton>(R.id.resetBtn)
        resetButton?.setOnClickListener {
            showResetCountDialog()
        }

        val saveButton = view.findViewById<ImageButton>(R.id.saveBtn)
        saveButton?.setOnClickListener {
            showSaveCountDialog()
        }

        initBottomSheetControls()
    }
    private fun initBottomSheetControls() {
        // When clicked, lower detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (detector.confthreshold >= 0.1) {
                detector.confthreshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (detector.confthreshold <= 0.8) {
                detector.confthreshold += 0.1f
                updateControlsUi()
            }
        }

        fragmentCameraBinding.bottomSheetLayout.thresholdIouMinus.setOnClickListener {
            if (detector.iouThreshold >= 0.1) {
                detector.iouThreshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise detection score threshold floor
        fragmentCameraBinding.bottomSheetLayout.thresholdIouPlus.setOnClickListener {
            if (detector.iouThreshold <= 0.8) {
                detector.iouThreshold += 0.1f
                updateControlsUi()
            }
        }

        // When clicked, reduce the number of objects that can be detected at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (detector.maxResults > 1) {
                detector.maxResults--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of objects that can be detected at a time
        fragmentCameraBinding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (detector.maxResults < 10) {
                detector.maxResults++
                updateControlsUi()
            }
        }

        // When clicked, decrease the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (detector.numThreadsUsed > 1) {
                detector.numThreadsUsed--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for detection
        fragmentCameraBinding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (detector.numThreadsUsed < 4) {
                detector.numThreadsUsed++
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // and GPU
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(0, false)
        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    detector.utilizeGPU = p2 == 1
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    private fun updateControlsUi() {
        fragmentCameraBinding.bottomSheetLayout.maxResultsValue.text =
            detector.maxResults.toString()
        fragmentCameraBinding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", detector.confthreshold)
        fragmentCameraBinding.bottomSheetLayout.thresholdIouValue.text =
            String.format("%.2f", detector.iouThreshold)
        fragmentCameraBinding.bottomSheetLayout.threadsValue.text =
            detector.numThreadsUsed.toString()

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        detector.clear()
        fragmentCameraBinding.overlay.clear()
    }

    private fun setupCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return

        val rotation = fragmentCameraBinding.viewFinder.display.rotation
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()
            .also { it.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider) }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .build()
            .also { it.setAnalyzer(cameraExecutor, ::analyzeImage) }

        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val bitmap = imageProxy.toBitmap() // Convert ImageProxy to Bitmap

        val rotatedBitmap = if (rotationDegrees != 0) {
            rotateBitmap(bitmap, rotationDegrees)
        } else {
            bitmap
        }
        detector.detect(rotatedBitmap)

        imageProxy.close()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    override fun onEmptyDetect() {
        if (isAdded) {
            fragmentCameraBinding.overlay.invalidate()
            fragmentCameraBinding.overlay.clear()
            clear()
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        uiScope.launch {
            fragmentCameraBinding.inferenceTime.text = "${inferenceTime}ms"
            fragmentCameraBinding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.CAMERA] == true &&
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startCamera()
        } else {

        }
    }

    private fun addCountPerClassListener(count: MutableMap.MutableEntry<String, Count>)
            : View.OnClickListener {
        return View.OnClickListener {
            val totalCountItem = totalCount[count.key]
            if (totalCountItem != null) {
                totalCountItem.count += count.value.count
                totalCount[count.key] = totalCountItem
            }
        }
    }

    private fun updateCount(newCounts: List<Count>) {
        requireActivity().runOnUiThread {
            for (item in newCounts) {
                val textViewToUpdate = countViews[item.name]
                textViewToUpdate?.text = "${item.name}: ${item.count}"
                countViews[item.name] = textViewToUpdate ?: countViews[item.name]!!
                counts[item.name]?.count = item.count
            }

            view?.invalidate()
        }
    }

    override fun onCountsUpdated(boundingBoxes: List<BoundingBox>) {
        val newCounts = counts.map { Count(it.value.name, 0) }.toMutableList()
        for (boundingBox in boundingBoxes) {
            val count = newCounts.find { it.name == boundingBox.clsName }
            count?.count = count?.count?.plus(1) ?: 0
        }

        updateCount(newCounts)
    }

    private fun showTotalCountDialog() {
        val totalCountView = LayoutInflater.from(requireContext())
            .inflate(R.layout.total_count_dialog, null)

        val linearLayout = totalCountView?.findViewById<LinearLayout>(R.id.countDialogContent)
        for (count in totalCount) {
            val textView = TextView(requireContext())
            textView.text = "${count.value.name}: ${count.value.count}"
            textView.textSize = 20f
            linearLayout?.addView(textView)
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(totalCountView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.create().show()
    }

    private fun showResetCountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to reset the count?")
            .setPositiveButton("OK") { dialog, _ ->
                resetTotalCount()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create().show()
    }

    private fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Please enable location permission")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showSaveCountDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to save the count?")
            .setPositiveButton("OK") { dialog, _ ->
                if (saveTotalCount())
                {
                    resetTotalCount()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create().show()
    }

    private fun resetTotalCount() {
        totalCount.forEach { (key, value) ->
            totalCount[key] = Count(value.name, 0)
        }
    }

    private fun saveTotalCount():Boolean {
        val locationResult = getLastLocation()

//        change to locationResult != null
        if (locationResult == null) {
//            val (latitude, longitude) = locationResult

            val newTree = Tree(
                latitude = 0.3232,
                longitude = 0.4323,
                isUploaded = false,
                ripe = totalCount["ripe"]?.count ?: 0,
                underripe = totalCount["underripe"]?.count ?: 0,
                unripe = totalCount["unripe"]?.count ?: 0,
                flower = totalCount["flower"]?.count ?: 0,
                abnromal = totalCount["abnormal"]?.count ?: 0
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    database.treeDao().insert(newTree)
                }
            }
            return true
        }
        showEnableLocationDialog()
        return false
    }

    private fun clear() {
        requireActivity().runOnUiThread {
            counts.forEach { (key, value) ->
                counts[key] = Count(value.name, 0)
            }
            for (item in counts) {
                val textViewToUpdate = countViews[item.value.name]
                textViewToUpdate?.text = "${item.value.name}: 0"
                countViews[item.value.name] = textViewToUpdate ?: countViews[item.value.name]!!
            }
            view?.invalidate()
        }
    }

    private fun sumCounts() {
        for (count in counts) {
            val totalCountItem = totalCount[count.value.name]
            if (totalCountItem != null) {
                totalCountItem.count += count.value.count
            }
        }
    }

    private fun getLastLocation(): Pair<Double, Double>? {
        var locationResult: Pair<Double, Double>? = null

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        locationResult = Pair(latitude, longitude)
                    }
                }
        }
        return locationResult
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}