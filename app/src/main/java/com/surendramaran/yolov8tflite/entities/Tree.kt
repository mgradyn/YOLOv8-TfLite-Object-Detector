package com.surendramaran.yolov8tflite.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import java.util.UUID

@Entity(tableName = "tree_table")
@IgnoreExtraProperties
data class Tree(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isUploaded: Boolean,
    val ripe: Int,
    val underripe: Int,
    val unripe: Int,
    val flower: Int,
    val abnormal: Int,
    val total: Int,
    val date: Long,
    @PrimaryKey val id: String = UUID.randomUUID().toString())