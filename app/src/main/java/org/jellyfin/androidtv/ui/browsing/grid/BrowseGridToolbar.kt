package org.jellyfin.androidtv.ui.browsing.grid

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import org.jellyfin.androidtv.R
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder

data class SortOption(
	val name: String,
	val value: ItemSortBy,
	val order: SortOrder
)

data class FilterState(
	val isUnwatchedOnly: Boolean = false,
	val isFavoriteOnly: Boolean = false
)

@Composable
fun BrowseGridToolbar(
	modifier: Modifier = Modifier,
	sortOptions: Map<Int, SortOption>,
	currentSortBy: ItemSortBy,
	filterState: FilterState,
	showUnwatchedFilter: Boolean = true,
	showLetterJump: Boolean = true,
	allowViewSelection: Boolean = true,
	onSortSelected: (SortOption) -> Unit,
	onUnwatchedToggle: () -> Unit,
	onFavoriteToggle: () -> Unit,
	onLetterJumpClick: () -> Unit,
	onSettingsClick: () -> Unit
) {
	var showSortMenu by remember { mutableStateOf(false) }

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		// Sort Button
		Box {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clickable { showSortMenu = true },
				contentAlignment = Alignment.Center
			) {
				Image(
					painter = painterResource(R.drawable.ic_sort),
					contentDescription = stringResource(R.string.lbl_sort_by),
					modifier = Modifier.size(26.dp)
				)
			}

			// Sort Menu Dropdown
			if (showSortMenu) {
				Popup(
					alignment = Alignment.TopEnd,
					onDismissRequest = { showSortMenu = false }
				) {
					Column(modifier = Modifier
						.background(Color.DarkGray, RoundedCornerShape(4.dp))
						.padding(8.dp)) {
						sortOptions.values.forEach { option ->
							Row(
								modifier = Modifier
									.clickable {
										onSortSelected(option)
										showSortMenu = false
									}
									.padding(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								if (option.value == currentSortBy) {
									BasicText("✓ ", style = TextStyle(color = Color.White, fontSize = 16.sp))
								} else {
									Spacer(Modifier.width(24.dp))
								}
								BasicText(option.name, style = TextStyle(color = Color.White, fontSize = 16.sp))
							}
						}
					}
				}
			}
		}

		// Unwatched Filter Button (conditional)
		if (showUnwatchedFilter) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clickable(onClick = onUnwatchedToggle),
				contentAlignment = Alignment.Center
			) {
				Image(
					painter = painterResource(R.drawable.ic_unwatch),
					contentDescription = stringResource(R.string.lbl_unwatched),
					modifier = Modifier.size(26.dp),
					colorFilter = ColorFilter.tint(
						if (filterState.isUnwatchedOnly) {
							Color.Blue
						} else {
							Color.White
						}
					)
				)
			}
		}

		// Favorite Filter Button
		Box(
			modifier = Modifier
				.size(48.dp)
				.clickable(onClick = onFavoriteToggle),
			contentAlignment = Alignment.Center
		) {
			Image(
				painter = painterResource(R.drawable.ic_heart),
				contentDescription = stringResource(R.string.lbl_favorite),
				modifier = Modifier.size(26.dp),
				colorFilter = ColorFilter.tint(
					if (filterState.isFavoriteOnly) {
						Color.Blue
					} else {
						Color.White
					}
				)
			)
		}

		// Letter Jump Button (conditional)
		if (showLetterJump) {
			Box(
				modifier = Modifier
					.size(48.dp)
					.clickable(onClick = onLetterJumpClick),
				contentAlignment = Alignment.Center
			) {
				Image(
					painter = painterResource(R.drawable.ic_jump_letter),
					contentDescription = stringResource(R.string.lbl_by_letter),
					modifier = Modifier.size(26.dp)
				)
			}
		}

		// Settings Button
		Box(
			modifier = Modifier
				.size(48.dp)
				.clickable(onClick = onSettingsClick),
			contentAlignment = Alignment.Center
		) {
			Image(
				painter = painterResource(R.drawable.ic_settings),
				contentDescription = stringResource(R.string.lbl_settings),
				modifier = Modifier.size(26.dp)
			)
		}
	}
}

// Preview для демонстрации
@Preview
@Composable
fun BrowseGridToolbarPreview() {
	val sortOptions = mapOf(
		0 to SortOption("Name", ItemSortBy.SORT_NAME, SortOrder.ASCENDING),
		1 to SortOption("Date Added", ItemSortBy.DATE_CREATED, SortOrder.DESCENDING),
		2 to SortOption("Premier Date", ItemSortBy.PREMIERE_DATE, SortOrder.DESCENDING)
	)

	var filterState by remember { mutableStateOf(FilterState()) }

	Box(modifier = Modifier
		.fillMaxWidth()
		.background(Color.Black)) {
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
			onSettingsClick = { /* Handle settings */ }
		)
	}
}
