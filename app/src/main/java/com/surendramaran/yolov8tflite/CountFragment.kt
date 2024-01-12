package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView

class CountFragment : Fragment(), Detector.CountListener {

    private lateinit var counts: MutableList<Count>
    private lateinit var countViews: MutableMap<String, TextView>
    private var totalCount: MutableList<Count> = mutableListOf(
        Count("flower", 0),
        Count("unripe", 0),
        Count("underripe", 0),
        Count("ripe", 0),
        Count("abnormal", 0)
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

        // Initialize counts
        counts = mutableListOf(
            Count("flower", 0),
            Count("unripe", 0),
            Count("underripe", 0),
            Count("ripe", 0),
            Count("abnormal", 0)
        )

        // Initialize the map to keep references to TextViews
        countViews = mutableMapOf()

        val gridLayout = view.findViewById<GridLayout>(R.id.counts_grid)

        // Loop through the counts list and create TextViews
        for ((index, count) in counts.withIndex()) {
            val textView = TextView(context)

            val layoutParams = GridLayout.LayoutParams()
            layoutParams.columnSpec = GridLayout.spec(index % 3)
            layoutParams.rowSpec = GridLayout.spec(index / 3)
            textView.layoutParams = layoutParams

            textView.text = "${count.name}: ${count.count}"
            textView.id = View.generateViewId()

            gridLayout.addView(textView, index)

            countViews[count.name] = textView
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countButton = view.findViewById(R.id.countBtn)
        countButton?.setOnClickListener {
            Log.d("CountFragment", "$counts")
        }
    }

    private fun updateCount(newCounts: List<Count>) {
        requireActivity().runOnUiThread {
            for (item in newCounts) {
                val countToUpdate = counts.find { it.name == item.name }
                countToUpdate?.count = item.count

                val textViewToUpdate = countViews[item.name]
                textViewToUpdate?.text = "${item.name}: ${item.count}"
                countViews[item.name] = textViewToUpdate ?: countViews[item.name]!!
                counts = newCounts.toMutableList()
            }

            (view?.parent as? View)?.invalidate()
        }
    }

    override fun onCountsUpdated(boundingBoxes: List<BoundingBox>) {
        val newCounts = counts.map { Count(it.name, 0) }.toMutableList()
        for (boundingBox in boundingBoxes) {
            val count = newCounts.find { it.name == boundingBox.clsName }
            count?.count = count?.count?.plus(1) ?: 0
        }

        updateCount(newCounts)
    }

    fun getCountButton(): Button? {
        return countButton
    }
    fun resetTotalCount() {
        totalCount = counts.map { Count(it.name, 0) }.toMutableList()
    }

    fun sumCounts() {
        val newCounts = totalCount.map { Count(it.name, 0) }.toMutableList()
        for (count in counts) {
            val totalCount = newCounts.find { it.name == count.name }
            totalCount?.count = totalCount?.count?.plus(count.count) ?: 0
        }
        totalCount = newCounts
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