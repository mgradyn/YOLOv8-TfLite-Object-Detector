package com.surendramaran.yolov8tflite.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tree_table")
data class Tree (val name: String,
                 val latitude: Double,
                 val longitude: Double,
                 val isUploaded: Boolean,
                 val ripe: Int,
                 val underripe: Int,
                 val unripe: Int,
                 val flower: Int,
                 val abnromal: Int,
                 @PrimaryKey val id: String = UUID.randomUUID().toString())