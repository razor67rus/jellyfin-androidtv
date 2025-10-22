package org.jellyfin.androidtv.ui.browsing.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.constant.GridDirection
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.constant.PosterSize
import org.jellyfin.androidtv.preference.LibraryPreferences
import org.jellyfin.sdk.model.api.BaseItemDto

class BrowseGridViewModel(
	private val folder: BaseItemDto,
	private val libraryPreferences: LibraryPreferences,
) : ViewModel() {

    private val _items = MutableStateFlow<List<Int>>(emptyList())
    val items = _items.asStateFlow()

	private val _posterSize = MutableStateFlow(libraryPreferences.get(LibraryPreferences.posterSize))
	val posterSize: StateFlow<PosterSize> = _posterSize.asStateFlow()

	private val _imageType = MutableStateFlow(libraryPreferences.get(LibraryPreferences.imageType))
	val imageType: StateFlow<ImageType> = _imageType.asStateFlow()

	private val _gridDirection = MutableStateFlow(libraryPreferences.get(LibraryPreferences.gridDirection))
	val gridDirection: StateFlow<GridDirection> = _gridDirection.asStateFlow()

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

    fun refreshPreferences() {
        _posterSize.value = libraryPreferences.get(LibraryPreferences.posterSize)
        _imageType.value = libraryPreferences.get(LibraryPreferences.imageType)
		_gridDirection.value = libraryPreferences.get(LibraryPreferences.gridDirection)
        loadItems() // Перезагружаем элементы с новыми настройками
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
