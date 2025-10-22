package org.jellyfin.androidtv.ui.browsing.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jellyfin.androidtv.preference.PreferencesRepository
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.card.ImageCard
import org.jellyfin.sdk.model.api.BaseItemDto
import org.koin.compose.koinInject


@Composable
fun BrowseGrid(
	folder: BaseItemDto
) {
	val preferencesRepository = koinInject<PreferencesRepository>()
	val libraryPreferences = preferencesRepository.getLibraryPreferences(folder.displayPreferencesId ?: "empty_preferences")

	val viewModel: BrowseGridViewModel = viewModel(factory = BrowseGridViewModelFactory(folder, libraryPreferences))
    val items by viewModel.items.collectAsStateWithLifecycle()



    Column(
        modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp)
    ) {
        Text(text = folder.name ?: "", color = Color.White, fontSize = 32.sp)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
				.weight(1f)
				.padding(top = 16.dp)
        ) {
            items(items) { item ->
                ImageCard(
                    onClick = {},
                    title = {
                        Column {
                            Text(
                                text = "Item $item",
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
}

//@Preview(device = Devices.TV_1080p)
//@Composable
//fun BrowseGridPreview() {
//    BrowseGrid(title = "Preview Title", folder = folder)
//}
