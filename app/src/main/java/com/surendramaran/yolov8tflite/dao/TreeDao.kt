package com.surendramaran.yolov8tflite.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.surendramaran.yolov8tflite.entities.Tree
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tree: Tree)

    @Update
    suspend fun update(tree: Tree)

    @Delete
    suspend fun delete(tree: Tree)

    @Query("delete from tree_table")
    fun deleteAllTrees()

    @Query("select * from tree_table")
    fun getAllTrees(): Flow<List<Tree>>
}