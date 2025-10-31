package org.jellyfin.androidtv.ui.browsing.grid

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.GridDirection
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.constant.PosterSize
import org.jellyfin.androidtv.data.repository.CustomMessageRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.PreferencesRepository
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.card.ImageCard
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import org.koin.compose.koinInject
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.ui.itemhandling.ItemLauncher
import timber.log.Timber


@Composable
fun BrowseGrid(
	folder: BaseItemDto
) {
	val preferencesRepository = koinInject<PreferencesRepository>()
	val userViewsRepository = koinInject<UserViewsRepository>()
	val customMessageRepository = koinInject<CustomMessageRepository>()
	val itemLauncher = koinInject<ItemLauncher>()
	val libraryPreferences = preferencesRepository.getLibraryPreferences(folder.displayPreferencesId ?: "empty_preferences")
	val allowViewSelection = userViewsRepository.allowViewSelection(folder.collectionType)

	val lifecycle = LocalLifecycleOwner.current.lifecycle
	val context = LocalContext.current
	val application = remember {context.applicationContext as Application}

	val viewModel: BrowseGridViewModel = viewModel(factory = BrowseGridViewModelFactory(application,folder, libraryPreferences, itemLauncher))
	val settingsLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) { result ->
			viewModel.refreshPreferences()
	}

	val items by viewModel.items.collectAsStateWithLifecycle()
	val posterSize by viewModel.posterSize.collectAsStateWithLifecycle()
	val imageType by viewModel.imageType.collectAsStateWithLifecycle()
	val gridDirection by viewModel.gridDirection.collectAsStateWithLifecycle()
	val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsStateWithLifecycle()
	val filterUnwatchedOnly by viewModel.filterUnwatchedOnly.collectAsStateWithLifecycle()
	val focusRequester = remember { FocusRequester() }
	val selectedIndex by viewModel.selectedIndex.collectAsStateWithLifecycle()
	val totalItems by viewModel.totalItems.collectAsStateWithLifecycle()
	val startLetter by viewModel.startLetter.collectAsStateWithLifecycle()
	val sortOption by viewModel.sortOptions.collectAsStateWithLifecycle()
	val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()

	LaunchedEffect(Unit) {
		viewModel.initializeAdapter(lifecycle)
	}

	LaunchedEffect(items) {
		if (items.isNotEmpty()) {
			focusRequester.requestFocus()
		}
	}

    Column(
        modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp)
    ) {
		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(text = folder.name ?: "", color = Color.White, fontSize = 32.sp)

			BrowseGridToolbar(
				sortOptions = sortOption.values.toList(),
				currentSortBy = sortBy ?: ItemSortBy.SORT_NAME,
				filterFavoritesOnly = filterFavoritesOnly,
				filterUnwatchedOnly = filterUnwatchedOnly,
				showUnwatchedFilter = true,
				showLetterJump = true,
				allowViewSelection = allowViewSelection,
				onSortSelected = { option ->
					viewModel.setSortBy(option)
				},
				onUnwatchedToggle = {
					viewModel.toggleUnwatchedOnly()
				},
				onFavoriteToggle = {
					viewModel.toggleFavoriteFilter()
				},
				onLetterJumpClick = { /* Handle letter jump */ },
				onSettingsClick = {
					settingsLauncher.launch(
						ActivityDestinations.displayPreferences(
							context,
							folder.displayPreferencesId ?: "empty_preferences",
							allowViewSelection
						)
					)
				}
			)

			when (gridDirection) {
				GridDirection.VERTICAL -> {
					VerticalBrowseGrid(
						items = items,
						posterSize = posterSize,
						imageType = imageType,
						focusRequester = focusRequester,
						onItemSelected = { index ->
							viewModel.setSelectedIndex(index)
						}
					)
				}

				GridDirection.HORIZONTAL -> {
					HorizontalBrowseGrid(
						items = items,
						posterSize = posterSize,
						imageType = imageType,
						focusRequester = focusRequester,
						onItemSelected = { index ->
							viewModel.setSelectedIndex(index)
						}
					)
				}
			}
		}

        StatusBar(
			folderName = folder.name,
            filterFavoritesOnly = filterFavoritesOnly,
            filterUnwatchedOnly = filterUnwatchedOnly,
            focusedIndex = selectedIndex,
            totalItems = totalItems,
			startLetter = startLetter,
			sortBy = sortBy,
			sortOptions = sortOption
        )
    }
}

@Composable
private fun VerticalBrowseGrid(
	items: List<BaseRowItem>,
	posterSize: PosterSize,
	imageType: ImageType,
	focusRequester: FocusRequester,
	onItemSelected: (Int) -> Unit,
	viewModel: BrowseGridViewModel = viewModel() // Получаем доступ к ViewModel
) {
	val columns = calculateColumns(posterSize, imageType)
	val context = LocalContext.current
	val imageHelper = koinInject<ImageHelper>()
	val gridState = rememberLazyGridState()

	var cellSize by remember { mutableStateOf(IntSize(100,150)) }

	LaunchedEffect(cellSize) {
		if (cellSize != null) {
			// Этот код выполнится, как только размер станет известен
			Timber.d("CellSize в пикселях: Ширина=${cellSize!!.width}, Высота=${cellSize!!.height}")
		}
	}

	// Отслеживаем прокрутку для пагинации
	LaunchedEffect(gridState, items) {
		snapshotFlow {
			gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
		}.collect { lastVisibleItem ->
			if (items.isNotEmpty() && lastVisibleItem >= items.size - columns * 2) {
				viewModel.loadMoreItemsIfNeeded(lastVisibleItem)
			}
		}
	}

	LazyVerticalGrid(
		columns = GridCells.Fixed(columns),
		state = gridState,
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.padding(top = 16.dp)

	) {
		itemsIndexed(items) { index, item ->
			CardPresenter(
				modifier = Modifier.onGloballyPositioned { coordinates ->
					// Этот блок выполняется после компоновки.
					// Сохраняем размер только для первой ячейки, чтобы избежать лишних пересчетов.
					if (index == 0) {
						cellSize = coordinates.size
					}
				},
				item = item,
				imageUrl = item.getImageUrl(context, imageHelper, imageType, 200,cellSize.height),
				index = index,
				onItemSelected = onItemSelected,
				focusRequester = focusRequester
			)
		}
	}
}

@Composable
private fun HorizontalBrowseGrid(
	items: List<BaseRowItem>,
	posterSize: PosterSize,
	imageType: ImageType,
	focusRequester: FocusRequester,
	onItemSelected: (Int) -> Unit,
	viewModel: BrowseGridViewModel = viewModel() // Получаем доступ к ViewModel
) {
	val rows = calculateRows(posterSize, imageType)
	val context = LocalContext.current
	val imageHelper = koinInject<ImageHelper>()
	val gridState = rememberLazyGridState()

	// Отслеживаем прокрутку для пагинации
	LaunchedEffect(gridState, items) {
		snapshotFlow {
			gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
		}.collect { lastVisibleItem ->
			if (items.isNotEmpty() && lastVisibleItem >= items.size - rows * 2) {
				viewModel.loadMoreItemsIfNeeded(lastVisibleItem)
			}
		}
	}

	LazyHorizontalGrid(
		rows = GridCells.Fixed(rows),
		state = gridState,
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier.padding(top = 16.dp)
	) {

		itemsIndexed(items) { index, item ->
			CardPresenter(
				modifier = Modifier,
				item = item,
				imageUrl = item.getImageUrl(context, imageHelper, imageType, 200,300),
				index = index,
				onItemSelected = onItemSelected,
				focusRequester = focusRequester,
			)
		}

	}
}

@Composable
private fun StatusBar(
	folderName: String?,
	filterFavoritesOnly: Boolean,
	filterUnwatchedOnly: Boolean,
	startLetter: String? = null,
	sortBy: ItemSortBy? = null,
	sortOptions: Map<Int, SortOption> = emptyMap(),
	focusedIndex: Int = 0,
	totalItems: Int = 0,
) {

	val sortOptionNameUnknown = SortOption(stringResource(R.string.lbl_bracket_unknown), ItemSortBy.SORT_NAME, SortOrder.ASCENDING)
	val sortOptionName = sortOptions.values.find { it.value == sortBy } ?: sortOptionNameUnknown

    Row(
        modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

		// Filter description
		Text(
			text = buildString {
				append(stringResource(R.string.lbl_showing))
				if (!filterFavoritesOnly && !filterUnwatchedOnly) append(" " + stringResource(R.string.lbl_all_items))
				if (filterUnwatchedOnly) append(" " + stringResource(R.string.lbl_unwatched))
				if (filterFavoritesOnly) append(" " + stringResource(R.string.lbl_favorites))
				if (startLetter != null) append(" " + stringResource(R.string.lbl_starting_with) + " " + startLetter)
				append(" " + stringResource(R.string.lbl_from) + " '" + folderName + "'")
				if (sortBy != null) append(" " + stringResource(R.string.lbl_sorted_by) + " " + sortOptionName.name)
			},
			color = Color.White,
			fontSize = 14.sp
		)

        // Counter
        Text(
            text = "${focusedIndex + 1}|$totalItems",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}


@Composable
private fun CardPresenter(
	modifier: Modifier,
	item: BaseRowItem,
	imageUrl: String?,
	index: Int,
	onItemSelected: (Int) -> Unit,
	focusRequester: FocusRequester,
	viewModel: BrowseGridViewModel = viewModel()
) {

	val context = LocalContext.current
	ImageCard(
		modifier = if (index == 0) modifier.focusRequester(focusRequester) else modifier,
		item = item,
		mainImageUrl = imageUrl,
		placeholder = ContextCompat.getDrawable(context, R.drawable.ic_movie),
		title = item.getCardName(context),
		contentText = item.getSubText(context),
		isFavorite = item.isFavorite,
		onFocus = { hasFocus ->
			if (hasFocus) {
				onItemSelected(index)
			}
		},
		onClick = {
			viewModel.onCardClicked(item)
		}
	)

}


private fun calculateColumns(posterSize: PosterSize, imageType: ImageType): Int {
	return when (posterSize) {
		PosterSize.SMALLEST -> when (imageType) {
			ImageType.BANNER -> 6
			ImageType.THUMB -> 11
			else -> 15
		}
		PosterSize.SMALL -> when (imageType) {
			ImageType.BANNER -> 5
			ImageType.THUMB -> 9
			else -> 13
		}
		PosterSize.MED -> when (imageType) {
			ImageType.BANNER -> 4
			ImageType.THUMB -> 7
			else -> 11
		}
		PosterSize.LARGE -> when (imageType) {
			ImageType.BANNER -> 3
			ImageType.THUMB -> 5
			else -> 7
		}
		PosterSize.X_LARGE -> when (imageType) {
			ImageType.BANNER -> 2
			ImageType.THUMB -> 3
			else -> 5
		}
	}
}

private fun calculateRows(posterSize: PosterSize, imageType: ImageType): Int {
	return when (posterSize) {
		PosterSize.SMALLEST -> when (imageType) {
			ImageType.BANNER -> 13
			ImageType.THUMB -> 7
			else -> 5
		}
		PosterSize.SMALL -> when (imageType) {
			ImageType.BANNER -> 11
			ImageType.THUMB -> 6
			else -> 4
		}
		PosterSize.MED -> when (imageType) {
			ImageType.BANNER -> 9
			ImageType.THUMB -> 5
			else -> 3
		}
		PosterSize.LARGE -> when (imageType) {
			ImageType.BANNER -> 7
			ImageType.THUMB -> 4
			else -> 2
		}
		PosterSize.X_LARGE -> when (imageType) {
			ImageType.BANNER -> 5
			ImageType.THUMB -> 2
			else -> 1
		}
	}
}



