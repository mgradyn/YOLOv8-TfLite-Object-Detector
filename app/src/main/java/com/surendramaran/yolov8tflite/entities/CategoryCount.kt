package com.surendramaran.yolov8tflite.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "count_category_table")
@IgnoreExtraProperties
data class CategoryCount (val total_count: Int,
                 @PrimaryKey val id: String)