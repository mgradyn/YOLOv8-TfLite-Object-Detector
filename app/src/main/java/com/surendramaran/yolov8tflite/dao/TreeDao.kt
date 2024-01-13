package com.surendramaran.yolov8tflite.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.surendramaran.yolov8tflite.entities.Tree

@Dao
interface TreeDao {
    @Insert
    fun insert(tree: Tree)

    @Update
    fun update(tree: Tree)

    @Delete
    fun delete(tree: Tree)

    @Query("delete from tree_table")
    fun deleteAllTrees()

    @Query("select * from tree_table")
    fun getAllTrees(): LiveData<List<Tree>>
}