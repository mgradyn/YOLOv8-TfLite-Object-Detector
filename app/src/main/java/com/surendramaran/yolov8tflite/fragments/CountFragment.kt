package com.surendramaran.yolov8tflite.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.surendramaran.yolov8tflite.BoundingBox
import com.surendramaran.yolov8tflite.Detector
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.entities.Tree
import com.surendramaran.yolov8tflite.model.Count
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountFragment : Fragment(), Detector.CountListener {

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onActivityCreatedCallback?.invoke()
        onActivityCreatedCallback = null
    }

    fun setOnActivityCreatedCallback(callback: () -> Unit) {
        onActivityCreatedCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        treeDao = TreeDatabase.getInstance(requireContext()).treeDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_count, container, false)

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        resetButton?.setOnClickListener {
            showSaveCountDialog()
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

    fun getButtonsLayout(): RelativeLayout? {
        return view?.findViewById(R.id.countButtonsLayout)
    }

    private fun resetTotalCount() {
        totalCount.forEach { (key, value) ->
            totalCount[key] = Count(value.name, 0)
        }
    }

    private fun saveTotalCount():Boolean {
        val locationResult = getLastLocation()

        if (locationResult != null) {
            val (latitude, longitude) = locationResult

            val newTree = Tree(
                latitude = latitude,
                longitude = longitude,
                isUploaded = false,
                ripe = totalCount["ripe"]?.count ?: 0,
                underripe = totalCount["underripe"]?.count ?: 0,
                unripe = totalCount["unripe"]?.count ?: 0,
                flower = totalCount["flower"]?.count ?: 0,
                abnromal = totalCount["abnormal"]?.count ?: 0
            )

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
//                    treeDao.insert(newTree)
                }
            }
            return true
        }
        showEnableLocationDialog()
        return false
    }

    fun clear() {
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

    private fun getLastLocation(): Pair<Double, Double>? {
        var locationResult: Pair<Double, Double>? = null

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
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
        @Volatile
        private var INSTANCE: CountFragment? = null

        fun getInstance(): CountFragment =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CountFragment().also {
                    INSTANCE = it
                }
            }
    }
}