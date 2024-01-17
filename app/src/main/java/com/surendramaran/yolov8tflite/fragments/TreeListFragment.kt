package com.surendramaran.yolov8tflite.fragments

import FileUtils.Companion.generateFile
import FileUtils.Companion.goToFileIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.surendramaran.yolov8tflite.R
import com.surendramaran.yolov8tflite.TreeApplication
import com.surendramaran.yolov8tflite.adapter.TreeCardAdapter
import com.surendramaran.yolov8tflite.entities.Tree
import com.surendramaran.yolov8tflite.model.CardItem
import java.io.File

class TreeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var treeCardAdapter: TreeCardAdapter
    private lateinit var treeList: List<Tree>
    private lateinit var btnExportToCsv: Button

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

        btnExportToCsv = view.findViewById(R.id.exportButton)
        btnExportToCsv.setOnClickListener {
            exportDatabaseToCSVFile()
        }

        return view
    }

    private fun exportDatabaseToCSVFile() {
        val csvFile = generateFile(requireContext(), "treeDatabase.csv")
        if (csvFile != null) {
            exportToCSVFile(csvFile)
            val intent = goToFileIntent(requireContext(), csvFile)
            startActivity(intent)
        }
    }
    private fun exportToCSVFile(csvFile: File) {
        csvWriter().open(csvFile, append = false) {
            writeRow(listOf("id", "name", "latitude", "longitude", "ripe", "underripe", "unripe", "flower", "abnormal", "total"))
            treeList.forEachIndexed { _, tree ->
                writeRow(listOf(tree.id, tree.name, tree.latitude, tree.longitude, tree.ripe, tree.underripe, tree.unripe, tree.flower, tree.abnromal, tree.total))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        treeViewModel.allTrees.observe(viewLifecycleOwner) { trees ->
            trees?.let {
                treeList = it
                treeCardAdapter.setCardItems(it)
            }
        }
    }
}