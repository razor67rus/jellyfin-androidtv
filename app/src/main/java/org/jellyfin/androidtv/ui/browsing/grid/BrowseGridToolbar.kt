package org.jellyfin.androidtv.ui.browsing.grid

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.sdk.model.api.CollectionType
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
	collectionType: CollectionType?,
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
	var showSortDialog by remember { mutableStateOf(false) }
	val context = LocalContext.current

	val sortOptions = getSortOptions(context, collectionType)

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
		horizontalArrangement = Arrangement.End,
		verticalAlignment = Alignment.CenterVertically
	) {
		// Sort Button
		ToolbarButton(
			iconRes = R.drawable.ic_sort,
			contentDescription = stringResource(R.string.lbl_sort_by),
			onClick = { showSortDialog = true }
		)

		// Unwatched Filter Button
		if (showUnwatchedFilter) {
			ToolbarButton(
				iconRes = R.drawable.ic_unwatch,
				contentDescription = stringResource(R.string.lbl_unwatched),
				isActive = filterState.isUnwatchedOnly,
				onClick = onUnwatchedToggle
			)
		}

		// Favorite Filter Button
		ToolbarButton(
			iconRes = R.drawable.ic_heart,
			contentDescription = stringResource(R.string.lbl_favorite),
			isActive = filterState.isFavoriteOnly,
			onClick = onFavoriteToggle
		)

		// Letter Jump Button
		if (showLetterJump) {
			ToolbarButton(
				iconRes = R.drawable.ic_jump_letter,
				contentDescription = stringResource(R.string.lbl_by_letter),
				onClick = onLetterJumpClick
			)
		}

		// Settings Button
		ToolbarButton(
			iconRes = R.drawable.ic_settings,
			contentDescription = stringResource(R.string.lbl_settings),
			onClick = onSettingsClick
		)
	}

	// Sort Dialog (более подходит для TV)
	if (showSortDialog) {
		SortDialog(
			sortOptions = sortOptions.values.toList(),
			currentSortBy = currentSortBy,
			onDismiss = { showSortDialog = false },
			onSortSelected = { option ->
				onSortSelected(option)
				showSortDialog = false
			}
		)
	}
}

@Composable
private fun SortDialog(
	sortOptions: List<SortOption>,
	currentSortBy: ItemSortBy,
	onDismiss: () -> Unit,
	onSortSelected: (SortOption) -> Unit
) {
//	val focusRequester = remember { FocusRequester() }

	// Автоматически фокусируемся на первом элементе
//	LaunchedEffect(Unit) {
//		focusRequester.requestFocus()
//	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(
			dismissOnBackPress = true,
			dismissOnClickOutside = true,
			usePlatformDefaultWidth = false
		)
	) {
		// Полупрозрачный фон
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(
					interactionSource = remember { MutableInteractionSource() },
					indication = null,
					onClick = onDismiss
				)
				.background(Color.Black.copy(alpha = 0.7f)),
			contentAlignment = Alignment.Center
		) {
			// Диалоговое окно
			Column(
				modifier = Modifier
					.width(400.dp)
					.background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
					.padding(24.dp)
			) {
				// Заголовок
				BasicText(
					text = stringResource(R.string.lbl_sort_by),
					style = TextStyle(
						color = Color.White,
						fontSize = 24.sp
					),
					modifier = Modifier.padding(bottom = 16.dp)
				)

				// Список опций сортировки
				sortOptions.forEachIndexed { index, option ->
					SortOptionItem(
						option = option,
						isSelected = option.value == currentSortBy,
//						modifier = if (index == 0) {
//							Modifier.focusRequester(focusRequester)
//						} else {
//							Modifier
//						},
						onClick = { onSortSelected(option) }
					)
				}
			}
		}
	}
}

@Composable
private fun SortOptionItem(
	option: SortOption,
	isSelected: Boolean,
	modifier: Modifier = Modifier,
	onClick: () -> Unit
) {
//	val interactionSource = remember { MutableInteractionSource() }
//	val isFocused by interactionSource.collectIsFocusedAsState()

	Row(
		modifier = modifier
//			.focusable(interactionSource = interactionSource)
			.clickable(
//				interactionSource = interactionSource,
//				indication = null,
				onClick = onClick
			)
			.background(
				when {
//					isFocused -> Color.White.copy(alpha = 0.3f)
					isSelected -> Color.White.copy(alpha = 0.1f)
					else -> Color.Transparent
				},
				RoundedCornerShape(8.dp)
			)
//			.border(
//				width = 3.dp,
////				color = if (isFocused) JellyfinTheme.colorScheme.onInputFocused else Color.Transparent,
//				shape = RoundedCornerShape(8.dp)
//			)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		// Радиокнопка
		Box(
			modifier = Modifier
				.size(24.dp)
				.border(
					width = 2.dp,
					color = if (isSelected) Color.Blue else Color.White,
					shape = RoundedCornerShape(12.dp)
				),
			contentAlignment = Alignment.Center
		) {
			if (isSelected) {
				Box(
					modifier = Modifier
						.size(12.dp)
						.background(Color.Blue, RoundedCornerShape(6.dp))
				)
			}
		}

		// Текст опции
		BasicText(
			text = option.name,
			style = TextStyle(
				color = Color.White,
				fontSize = 18.sp
			),
			modifier = Modifier.padding(start = 12.dp)
		)
	}
}

@Composable
private fun ToolbarButton(
	iconRes: Int,
	contentDescription: String,
	isActive: Boolean = false,
	onClick: () -> Unit
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Box(
		modifier = Modifier
			.size(32.dp)
			.background(
				if (isFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
				RoundedCornerShape(4.dp)
			)
			.clickable(
				interactionSource = interactionSource,
				indication = null,
				onClick = onClick
			)
			.focusable(interactionSource = interactionSource),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = painterResource(iconRes),
			contentDescription = contentDescription,
			modifier = Modifier.size(26.dp),
			colorFilter = ColorFilter.tint(
				if (isActive) Color.Blue else Color.White
			)
		)
	}
}

private fun getSortOptions(context: Context, collectionType: CollectionType?): Map<Int, SortOption> {
	val sortOptions = mutableMapOf(
		0 to SortOption(context.getString(R.string.lbl_name), ItemSortBy.SORT_NAME, SortOrder.ASCENDING),
		1 to SortOption(context.getString(R.string.lbl_date_added), ItemSortBy.DATE_CREATED, SortOrder.DESCENDING),
		2 to SortOption(context.getString(R.string.lbl_premier_date), ItemSortBy.PREMIERE_DATE, SortOrder.DESCENDING),
		3 to SortOption(context.getString(R.string.lbl_rating), ItemSortBy.OFFICIAL_RATING, SortOrder.ASCENDING),
		4 to SortOption(context.getString(R.string.lbl_community_rating), ItemSortBy.COMMUNITY_RATING, SortOrder.DESCENDING),
		5 to SortOption(context.getString(R.string.lbl_critic_rating), ItemSortBy.CRITIC_RATING, SortOrder.DESCENDING)
	)

	if (collectionType == CollectionType.TVSHOWS) {
		sortOptions[6] = SortOption(context.getString(R.string.lbl_last_played), ItemSortBy.SERIES_DATE_PLAYED, SortOrder.DESCENDING)
	} else {
		sortOptions[6] = SortOption(context.getString(R.string.lbl_last_played), ItemSortBy.DATE_PLAYED, SortOrder.DESCENDING)
	}

	if (collectionType == CollectionType.MOVIES) {
		sortOptions[7] = SortOption(context.getString(R.string.lbl_runtime), ItemSortBy.RUNTIME, SortOrder.ASCENDING)
	}

	return sortOptions
}
