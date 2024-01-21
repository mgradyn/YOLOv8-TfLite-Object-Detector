package com.surendramaran.yolov8tflite

import androidx.annotation.WorkerThread
import com.surendramaran.yolov8tflite.database.TreeDao
import com.surendramaran.yolov8tflite.entities.Tree
import kotlinx.coroutines.flow.Flow

class TreeRepository(private val treeDao: TreeDao) {

    val allTrees: Flow<List<Tree>> = treeDao.getAllTrees()

    @WorkerThread
    fun getAllTreeIds(): Flow<List<String>> {
        return treeDao.getAllTreeIds()
    }

    @WorkerThread
    suspend fun insert(tree: Tree) {
        treeDao.insert(tree)
    }

    @WorkerThread
    suspend fun update(tree: Tree) {
        treeDao.update(tree)
    }

    @WorkerThread
    suspend fun delete(tree: Tree) {
        treeDao.delete(tree)
    }


}