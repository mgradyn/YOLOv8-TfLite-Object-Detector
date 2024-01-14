package com.surendramaran.yolov8tflite.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.adapter.TreeCardAdapter
import com.surendramaran.yolov8tflite.model.CardItem

class TreeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var treeCardAdapter: TreeCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tree_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val cardItems = generateDummyData()
        treeCardAdapter = TreeCardAdapter(cardItems)
        recyclerView.adapter = treeCardAdapter

        return view
    }

    private fun generateDummyData(): List<CardItem> {
        return listOf(
            CardItem("Card 1", "Content 1"),
            CardItem("Card 2", "Content 2"),
            CardItem("Card 3", "Content 3"),
        )
    }
}