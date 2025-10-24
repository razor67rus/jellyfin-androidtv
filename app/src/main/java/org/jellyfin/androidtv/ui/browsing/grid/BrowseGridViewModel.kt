package org.jellyfin.androidtv.ui.browsing.grid

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
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
import org.jellyfin.androidtv.constant.QueryType
import org.jellyfin.androidtv.data.model.FilterOptions
import org.jellyfin.androidtv.preference.LibraryPreferences
import org.jellyfin.androidtv.ui.browsing.BrowseGridFragment
import org.jellyfin.androidtv.ui.browsing.BrowseRowDef
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.ItemRowAdapter
import org.jellyfin.androidtv.ui.itemhandling.ItemRowAdapterWrapper
import org.jellyfin.androidtv.ui.presentation.CardPresenter
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber

class BrowseGridViewModel(
	application: Application,
	private val folder: BaseItemDto,
	private val libraryPreferences: LibraryPreferences,
) : AndroidViewModel(application) {

	private val context: Context get() = getApplication<Application>().applicationContext

	private var adapterWrapper: ItemRowAdapterWrapper? = null

    private val _itemsTest = MutableStateFlow<List<Int>>(emptyList())
    val itemsTest = _itemsTest.asStateFlow()

	private val _posterSize = MutableStateFlow(libraryPreferences[LibraryPreferences.posterSize])
	val posterSize: StateFlow<PosterSize> = _posterSize.asStateFlow()

	private val _imageType = MutableStateFlow(libraryPreferences[LibraryPreferences.imageType])
	val imageType: StateFlow<ImageType> = _imageType.asStateFlow()

	private val _gridDirection = MutableStateFlow(libraryPreferences[LibraryPreferences.gridDirection])
	val gridDirection: StateFlow<GridDirection> = _gridDirection.asStateFlow()

	private val _items = MutableStateFlow<List<BaseRowItem>>(emptyList())
	val items: StateFlow<List<BaseRowItem>> = _items.asStateFlow()

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	private val _totalItems = MutableStateFlow(0)
	val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

    init {
		loadItemsTest()
    }

	fun initializeAdapter(
		cardPresenter: CardPresenter,
		rowDef: BrowseRowDef,
		chunkSize: Int,
		lifecycle: Lifecycle
	) {
		val adapter = createAdapter(cardPresenter, rowDef, chunkSize)
		adapterWrapper = ItemRowAdapterWrapper(adapter, viewModelScope, lifecycle)

		viewModelScope.launch {
			adapterWrapper?.items?.collect {
				_items.value = it
				Timber.d("Updated items: ${it.size} items in viewModel")
			}
		}
		viewModelScope.launch {
			adapterWrapper?.isLoading?.collect {
				_isLoading.value = it
			}
		}
		viewModelScope.launch {
			adapterWrapper?.totalItems?.collect {
				_totalItems.value = it
			}
		}

		// Загружаем начальные данные
		adapterWrapper?.retrieve()
	}

	private fun createAdapter(
		cardPresenter: CardPresenter,
		rowDef: BrowseRowDef,
		chunkSize: Int
	): ItemRowAdapter {

		val adapter = when (rowDef.queryType) {
			QueryType.NextUp -> ItemRowAdapter(
				context,
				rowDef.nextUpQuery,
				true,
				cardPresenter,
				null
			)
			QueryType.Artists -> ItemRowAdapter(
				context,
				rowDef.artistsQuery,
				chunkSize,
				cardPresenter,
				null
			)
			QueryType.AlbumArtists -> ItemRowAdapter(
				context,
				rowDef.albumArtistsQuery,
				chunkSize,
				cardPresenter,
				null
			)
			else -> ItemRowAdapter(
				context,
				rowDef.query,
				chunkSize,
				rowDef.preferParentThumb,
				rowDef.isStaticHeight,
				cardPresenter,
				null
			)
		}
		// Применяем сохраненные фильтры и сортировку
		val filters = FilterOptions().apply {
			isFavoriteOnly = libraryPreferences[LibraryPreferences.filterFavoritesOnly]
			isUnwatchedOnly = libraryPreferences[LibraryPreferences.filterUnwatchedOnly]
		}
		adapter.filters = filters

		val sortBy = libraryPreferences[LibraryPreferences.sortBy]
		val sortOrder = libraryPreferences[LibraryPreferences.sortOrder]
		adapter.setSortBy(BrowseGridFragment.SortOption("", sortBy, sortOrder))

		return adapter
	}

    fun loadItemsTest() {
        viewModelScope.launch {
			_itemsTest.value = List(20) { it }
        }
    }

    fun refreshPreferences() {
        _posterSize.value = libraryPreferences[LibraryPreferences.posterSize]
        _imageType.value = libraryPreferences[LibraryPreferences.imageType]
		_gridDirection.value = libraryPreferences[LibraryPreferences.gridDirection]
		loadItemsTest() // Перезагружаем элементы с новыми настройками
    }
}

class BrowseGridViewModelFactory(
	private val application: Application,
	private val folder: BaseItemDto,
	private val libraryPreferences: LibraryPreferences
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BrowseGridViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return BrowseGridViewModel(
				application,
				folder,
				libraryPreferences
			) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
