package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView

class CountFragment : Fragment() {

    private lateinit var counts: MutableList<Count>
    private lateinit var countViews: MutableMap<String, TextView>

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

    fun updateCount(name: String, newCount: Int) {
        // Find the Count object and update its value
        val countToUpdate = counts.find { it.name == name }
        countToUpdate?.count = newCount

        // Find the corresponding TextView and update its text
        val textViewToUpdate = countViews[name]
        textViewToUpdate?.text = "$name: $newCount"
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