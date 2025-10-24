package org.jellyfin.androidtv.ui.browsing.grid

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jellyfin.androidtv.constant.GridDirection
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.constant.PosterSize
import org.jellyfin.androidtv.data.repository.CustomMessageRepository
import org.jellyfin.androidtv.data.repository.UserViewsRepository
import org.jellyfin.androidtv.preference.PreferencesRepository
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.card.ImageCard
import org.jellyfin.androidtv.ui.browsing.BrowseRowDef
import org.jellyfin.androidtv.ui.browsing.BrowsingUtils
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.navigation.ActivityDestinations
import org.jellyfin.androidtv.ui.presentation.CardPresenter
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder
import org.koin.compose.koinInject


@Composable
fun BrowseGrid(
	folder: BaseItemDto
) {
	val preferencesRepository = koinInject<PreferencesRepository>()
	val userViewsRepository = koinInject<UserViewsRepository>()
	val customMessageRepository = koinInject<CustomMessageRepository>()
	val libraryPreferences = preferencesRepository.getLibraryPreferences(folder.displayPreferencesId ?: "empty_preferences")
	val allowViewSelection = userViewsRepository.allowViewSelection(folder.collectionType)

	val lifecycle = LocalLifecycleOwner.current.lifecycle
	val context = LocalContext.current
	val application = remember {context.applicationContext as Application}

	val viewModel: BrowseGridViewModel = viewModel(factory = BrowseGridViewModelFactory(application,folder, libraryPreferences))
	val settingsLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) { result ->
			viewModel.refreshPreferences()
	}

	val items by viewModel.items.collectAsStateWithLifecycle()
	val itemsTest by viewModel.itemsTest.collectAsStateWithLifecycle()
	val posterSize by viewModel.posterSize.collectAsStateWithLifecycle()
	val imageType by viewModel.imageType.collectAsStateWithLifecycle()
	val gridDirection by viewModel.gridDirection.collectAsStateWithLifecycle()

	var filterState by remember { mutableStateOf(FilterState()) }

	val sortOptions = mapOf(
		0 to SortOption("Name", ItemSortBy.SORT_NAME, SortOrder.ASCENDING),
		1 to SortOption("Date Added", ItemSortBy.DATE_CREATED, SortOrder.DESCENDING),
		2 to SortOption("Premier Date", ItemSortBy.PREMIERE_DATE, SortOrder.DESCENDING)
	)


	LaunchedEffect(Unit) {
		val cardHeight = 200 // Calculate based on screen size
		val cardPresenter = CardPresenter(false, ImageType.POSTER, cardHeight)
		cardPresenter.setUniformAspect(true)

		val rowDef = BrowseRowDef(
			"",
			BrowsingUtils.createBrowseGridItemsRequest(folder),
			100,
			false,
			true
		)

		viewModel.initializeAdapter(cardPresenter, rowDef, 100, lifecycle)
	}

    Column(
        modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp)
    ) {
        Text(text = folder.name ?: "", color = Color.White, fontSize = 32.sp)

		BrowseGridToolbar(
			sortOptions = sortOptions,
			currentSortBy = ItemSortBy.SORT_NAME,
			filterState = filterState,
			showUnwatchedFilter = true,
			showLetterJump = true,
			allowViewSelection = true,
			onSortSelected = { /* Handle sort */ },
			onUnwatchedToggle = {
				filterState = filterState.copy(
					isUnwatchedOnly = !filterState.isUnwatchedOnly
				)
			},
			onFavoriteToggle = {
				filterState = filterState.copy(
					isFavoriteOnly = !filterState.isFavoriteOnly
				)
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
					imageType = imageType
				)
			}
			GridDirection.HORIZONTAL -> {
				HorizontalBrowseGrid(
					items = items,
					posterSize = posterSize,
					imageType = imageType
				)
			}
		}
    }
}

@Composable
private fun VerticalBrowseGrid(
	items:  List<BaseRowItem>,
	posterSize: PosterSize,
	imageType: ImageType,
) {
	val columns = calculateColumns(posterSize, imageType)

	LazyVerticalGrid(
		columns = GridCells.Fixed(columns),
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier
			.padding(top = 16.dp)
	) {
		items(items) { item ->
			ImageCard(
				onClick = {},
				title = {
					Column {
						Text(
							text = "${item.baseItem?.name}",
							color = Color.White,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						Text(text = "2023", color = Color.Gray)
					}
				},
				image = {
					Box(
						modifier = Modifier
							.background(Color.DarkGray)
							.aspectRatio(2f / 3f)
					)
				}
			)
		}
	}
}

@Composable
private fun HorizontalBrowseGrid(
	items: List<BaseRowItem>,
	posterSize: PosterSize,
	imageType: ImageType,

) {
	val rows = calculateRows(posterSize, imageType)

	LazyHorizontalGrid(
		rows = GridCells.Fixed(rows),
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
//		verticalArrangement = Arrangement.spacedBy(8.dp),
		modifier = Modifier
//			.fillMaxSize()
			.padding(top = 16.dp)
	) {

		items(items) { item ->
			ImageCard(
				onClick = {},
				title = {
					Column {
						Text(
							text = "${item.baseItem?.name}",
							color = Color.White,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						Text(text = "2023", color = Color.Gray)
					}
				},
				image = {
					Box(
						modifier = Modifier
							.width(60.dp)
							.background(Color.DarkGray)
							.aspectRatio(2f / 3f)
					)
				}
			)
		}

	}
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

//@Preview(device = Devices.TV_1080p)
//@Composable
//fun BrowseGridPreview() {
//    BrowseGrid(title = "Preview Title", folder = folder)
//}
