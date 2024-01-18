package com.surendramaran.yolov8tflite.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.surendramaran.yolov8tflite.TreeRepository
import com.surendramaran.yolov8tflite.entities.Tree
import kotlinx.coroutines.launch

class TreeViewModel(private val repository: TreeRepository) : ViewModel() {
    val allTrees: LiveData<List<Tree>> = repository.allTrees.asLiveData()

    fun insert(tree: Tree) = viewModelScope.launch {
        repository.insert(tree)
    }
    fun update(tree: Tree) = viewModelScope.launch {
        repository.update(tree)
    }
}

class TreeViewModelFactory(private val repository: TreeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TreeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TreeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}