package com.surendramaran.yolov8tflite

import com.surendramaran.yolov8tflite.entities.Tree

interface FirebaseCallback {
    fun addData(treeEntity: Tree)
}