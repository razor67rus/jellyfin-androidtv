package org.jellyfin.androidtv.ui.browsing.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.preference.LibraryPreferences
import org.jellyfin.sdk.model.api.BaseItemDto

class BrowseGridViewModel(
	private val folder: BaseItemDto,
	private val libraryPreferences: LibraryPreferences,
) : ViewModel() {

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

class BrowseGridViewModelFactory(
	private val folder: BaseItemDto,
	private val libraryPreferences: LibraryPreferences
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BrowseGridViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return BrowseGridViewModel(
				folder,
				libraryPreferences
			) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
