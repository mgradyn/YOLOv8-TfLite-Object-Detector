package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CountFragment : Fragment() {

    private lateinit var counts: MutableList<Count>

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

        // TODO: Use classifications to update the UI of the fragment

        return view
    }
}