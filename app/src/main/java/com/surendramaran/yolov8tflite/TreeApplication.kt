package com.surendramaran.yolov8tflite

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TreeApplication: Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { TreeDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TreeRepository(database.treeDao()) }
}