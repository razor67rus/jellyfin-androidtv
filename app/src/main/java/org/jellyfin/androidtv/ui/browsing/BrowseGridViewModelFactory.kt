package org.jellyfin.androidtv.ui.browsing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BrowseGridViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BrowseGridViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BrowseGridViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
