package com.surendramaran.yolov8tflite.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.TreeApplication
import com.surendramaran.yolov8tflite.adapter.TreeCardAdapter
import com.surendramaran.yolov8tflite.model.CardItem

class TreeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var treeCardAdapter: TreeCardAdapter

    private val treeViewModel: TreeViewModel by viewModels {
        TreeViewModelFactory((requireActivity().application as TreeApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tree_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        treeCardAdapter = TreeCardAdapter()
        recyclerView.adapter = treeCardAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        treeViewModel.allTrees.observe(viewLifecycleOwner) { trees ->
            trees?.let {
                treeCardAdapter.setCardItems(it)
            }
        }
    }

    private fun generateDummyData(): List<CardItem> {
        return listOf(
            CardItem("Card 1", "Content 1"),
            CardItem("Card 2", "Content 2"),
            CardItem("Card 3", "Content 3"),
        )
    }
}