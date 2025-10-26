package org.jellyfin.androidtv.ui.itemhandling

import android.content.Context
import androidx.leanback.widget.ObjectAdapter
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.constant.QueryType
import org.jellyfin.androidtv.data.model.FilterOptions
import org.jellyfin.androidtv.preference.LibraryPreferences
import org.jellyfin.androidtv.ui.browsing.BrowseGridFragment
import org.jellyfin.androidtv.ui.browsing.BrowseRowDef
import org.jellyfin.androidtv.ui.presentation.CardPresenter
import org.jellyfin.androidtv.util.apiclient.EmptyResponse
import org.jellyfin.sdk.model.api.ItemSortBy
import timber.log.Timber

// Исправленный wrapper для ItemRowAdapter
class ItemRowAdapterWrapper(
	private val adapter: ItemRowAdapter,
	private val coroutineScope: CoroutineScope,
	private val lifecycle: Lifecycle
) {
	private val _items = MutableStateFlow<List<BaseRowItem>>(emptyList())
	val items: StateFlow<List<BaseRowItem>> = _items.asStateFlow()

	private val _isLoading = MutableStateFlow(false)
	val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

	private val _totalItems = MutableStateFlow(0)
	val totalItems: StateFlow<Int> = _totalItems.asStateFlow()

	private val _error = MutableStateFlow<String?>(null)
	val error: StateFlow<String?> = _error.asStateFlow()

	// Для Leanback используем DataObserver, не AdapterDataObserver
	private val dataObserver = object : ObjectAdapter.DataObserver() {
		override fun onChanged() {
			Timber.d("Adapter data changed")
			updateItems()
		}

		override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
			Timber.d("Item range changed: start=$positionStart, count=$itemCount")
			updateItems()
		}

		override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
			Timber.d("Item range inserted: start=$positionStart, count=$itemCount")
			updateItems()
		}

		override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
			Timber.d("Item range removed: start=$positionStart, count=$itemCount")
			updateItems()
		}
	}

	init {
		// Регистрируем observer для Leanback адаптера
		adapter.registerObserver(dataObserver)

		// Слушаем завершение загрузки
		adapter.setRetrieveFinishedListener(object : EmptyResponse(lifecycle) {
			override fun onResponse() {
				_isLoading.value = false
				_totalItems.value = adapter.totalItems
				_error.value = null
				Timber.d("Retrieve finished: itemsLoaded=${adapter.itemsLoaded}, total=${adapter.totalItems}")
				updateItems()
			}

			override fun onError(exception: Exception) {
				_isLoading.value = false
				_error.value = exception.message
				Timber.e(exception, "Failed to retrieve items")
			}
		})
	}

	private fun updateItems() {
		val newItems = mutableListOf<BaseRowItem>()
		for (i in 0 until adapter.size()) {
			when (val item = adapter.get(i)) {
				is BaseRowItem -> newItems.add(item)
			}
		}
		Timber.d("Updated items: ${newItems.size} items in wrapper")
		_items.value = newItems
	}

	fun retrieve() {
		Timber.d("Starting initial retrieve")
		_isLoading.value = true
		_error.value = null
		adapter.Retrieve()
	}

	fun loadMoreItemsIfNeeded(position: Int) {
		if (_isLoading.value) {
			Timber.d("Already loading, skipping loadMoreItemsIfNeeded")
			return
		}

		Timber.d("Calling adapter.loadMoreItemsIfNeeded for position $position")
		adapter.loadMoreItemsIfNeeded(position)
	}

	fun setSortBy(sortOption: BrowseGridFragment.SortOption) {
		adapter.setSortBy(sortOption)
	}

	fun setFilters(filters: FilterOptions) {
		adapter.filters = filters
	}

	fun setStartLetter(letter: String?) {
		adapter.startLetter = letter
	}

	fun getFilters(): FilterOptions? = adapter.filters

	fun getSortBy(): ItemSortBy = adapter.sortBy

	fun getStartLetter(): String? = adapter.startLetter

	fun reRetrieveIfNeeded(): Boolean {
		return adapter.ReRetrieveIfNeeded()
	}

	fun refreshItem(item: BaseRowItem, onComplete: () -> Unit) {
		coroutineScope.launch {
			// Логика обновления элемента
			onComplete()
		}
	}

	fun cleanup() {
		// Отписываемся от observer при уничтожении
		adapter.unregisterObserver(dataObserver)
	}

	companion object {
		fun createAdapter(
			context: Context,
			libraryPreferences: LibraryPreferences,
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
	}
}
