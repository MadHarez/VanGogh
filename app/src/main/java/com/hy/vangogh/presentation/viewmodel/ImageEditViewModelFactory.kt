package com.hy.vangogh.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ImageEditViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImageEditViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
