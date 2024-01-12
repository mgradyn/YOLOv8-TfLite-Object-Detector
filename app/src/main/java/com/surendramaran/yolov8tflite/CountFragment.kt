package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

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
    private lateinit var drawerLayout: DrawerLayout

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

        countViews = mutableMapOf()
        val gridLayout = view.findViewById<GridLayout>(R.id.counts_grid)

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
            sumCounts()
            Log.d("CountFragment", "Total Count: $totalCount")
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

            view?.invalidate()
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

    fun clear() {
        requireActivity().runOnUiThread {
            counts = counts.map { Count(it.name, 0) }.toMutableList()
            for (item in counts) {
                val textViewToUpdate = countViews[item.name]
                textViewToUpdate?.text = "${item.name}: 0"
                countViews[item.name] = textViewToUpdate ?: countViews[item.name]!!
            }
            view?.invalidate()
        }
    }

    private fun sumCounts() {
        val newCounts = mutableListOf<Count>()
        for (count in counts) {
            val totalCountItem = totalCount.find { it.name == count.name }
            if (totalCountItem != null) {
                totalCountItem.count += count.count
                newCounts.add(totalCountItem)
            }
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