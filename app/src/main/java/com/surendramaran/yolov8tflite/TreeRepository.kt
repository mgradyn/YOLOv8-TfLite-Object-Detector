package com.surendramaran.yolov8tflite

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.entities.Tree

class TreeRepository(private val treeDao: TreeDao) {

    val allWords: LiveData<List<Tree>> = treeDao.getAllTrees()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(tree: Tree) {
        treeDao.insert(tree)
    }
}