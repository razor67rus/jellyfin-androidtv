package org.jellyfin.androidtv.ui.browsing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowseGridViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<Int>>(emptyList())
    val items = _items.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            // Здесь будет ваша логика загрузки данных
            // А пока используем тестовые данные
            _items.value = List(20) { it }
        }
    }
}
