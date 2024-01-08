package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CountFragment : Fragment() {

    private lateinit var classifications: MutableList<Classification>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_count, container, false)

        // Initialize classifications
        classifications = mutableListOf(
            Classification("flower", 5),
            Classification("unripe", 2),
            Classification("underripe", 3),
            Classification("ripe", 4),
            Classification("abnormal", 1)
        )

        // TODO: Use classifications to update the UI of the fragment

        return view
    }
}