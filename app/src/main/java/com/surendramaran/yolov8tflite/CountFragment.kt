package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

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
    private var countButton: Button? = null
    private var onActivityCreatedCallback: (() -> Unit)? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onActivityCreatedCallback?.invoke()
        onActivityCreatedCallback = null
    }

    fun setOnActivityCreatedCallback(callback: () -> Unit) {
        onActivityCreatedCallback = callback
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
            var countClass = count.value.name
            var countAmount = count.value.count

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
        countButton = view.findViewById(R.id.countBtn)
        countButton?.setOnClickListener {
            sumCounts()
            Log.d("CountFragment", "Total Count: $totalCount")
        }

        val totalCountButton = view.findViewById<Button>(R.id.totalCountBtn)
        totalCountButton?.setOnClickListener {
            showTotalCountDialog()
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

    fun getCountButton(): Button? {
        return countButton
    }

    fun resetTotalCount() {
        totalCount.forEach { (key, value) ->
            totalCount[key] = Count(value.name, 0)
        }
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