package org.jellyfin.androidtv.ui.browsing

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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.card.ImageCard

@Composable
fun BrowseGrid(
    title: String,
    viewModel: BrowseGridViewModel = viewModel(factory = BrowseGridViewModelFactory())
) {

    val items by viewModel.items.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(text = title)

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

@Preview(device = Devices.TV_1080p)
@Composable
fun BrowseGridPreview() {
    BrowseGrid(title = "Preview Title")
}
