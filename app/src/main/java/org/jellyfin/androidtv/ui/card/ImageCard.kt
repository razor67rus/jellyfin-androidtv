package org.jellyfin.androidtv.ui.card

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.composable.AsyncImage
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.util.getActivity
import org.jellyfin.sdk.model.api.BaseItemKind
import java.text.NumberFormat

@Composable
fun ImageCard(
	modifier: Modifier,
	showInfo: Boolean = true,
	item: BaseRowItem? = null,
	mainImageUrl: String? = null,
	aspectRatio: Float = 2f / 3f,
	scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
	placeholder: Drawable? = null,
	title: String? = null,
	contentText: String? = null,
	rating: String? = null,
	unwatchedCount: Int = -1,
	progress: Int = 0,
	isPlaying: Boolean = false,
	isFavorite: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
	onFocus: (isFocused: Boolean) -> Unit = {}
) {
    val context = LocalContext.current
	val nf = remember { NumberFormat.getInstance() }
	val interactionSource = remember { MutableInteractionSource() }

	val isFocused by interactionSource.collectIsFocusedAsState()
	val borderColor = if (isFocused) {
		JellyfinTheme.colorScheme.onInputFocused
	} else {
		Color.Transparent
	}
	val cardShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
			.onFocusChanged { onFocus(it.isFocused) }
			.clickable(
				onClick = onClick,
				indication = null,
				interactionSource = interactionSource,
			)
			.focusable(interactionSource = interactionSource)

    ) {
        Column {
            Box(
                modifier = Modifier
					.fillMaxWidth()
					.border(3.dp, borderColor, cardShape)
            ) {
                // Main Image
                AsyncImage(
                    url = mainImageUrl,
					placeholder = placeholder,
					scaleType = scaleType,
                    modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(aspectRatio)
						.clip(cardShape)
                )

                // Overlay
                if (!showInfo && item != null && item.showCardInfoOverlay) {
                    Box(
                        modifier = Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.background(Color.Black.copy(alpha = 0.7f))
							.padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconRes = when (item.baseItem?.type) {
                                BaseItemKind.PHOTO -> R.drawable.ic_camera
                                BaseItemKind.PHOTO_ALBUM -> R.drawable.ic_photos
                                BaseItemKind.VIDEO -> R.drawable.ic_movie
                                BaseItemKind.FOLDER -> R.drawable.ic_folder
                                else -> null
                            }

                            if (iconRes != null) {
                                Image(
                                    painter = painterResource(iconRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = "${item.getFullName(context)}",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (item is BaseItemDtoBaseRowItem) {
                                Text(
                                    text = item.childCountStr ?: "",
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Progress bar
                if (progress > 0) {
                    Box(
                        modifier = Modifier
							.align(Alignment.BottomCenter)
							.fillMaxWidth()
							.height(2.dp)
							.background(Color.Gray)
                    ) {
                        Box(
                            modifier = Modifier
								.fillMaxHeight()
								.fillMaxWidth(progress / 100f)
								.background(Color.White)
                        )
                    }
                }

                // Watched indicator
                if (unwatchedCount >= 0) {
                    Box(
                        modifier = Modifier
							.align(Alignment.TopEnd)
							.padding(4.dp)
                    ) {
                        if (unwatchedCount > 0) {
                            Text(
                                text = if (unwatchedCount > 99)
                                    stringResource(R.string.watch_count_overflow)
                                else
                                    nf.format(unwatchedCount),
                                color = Color.White,
                                modifier = Modifier
									.background(
										Color.Black.copy(alpha = 0.7f),
										RoundedCornerShape(4.dp)
									)
									.padding(4.dp)
                            )
                        } else if (unwatchedCount == 0) {
                            Image(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Playing indicator
                if (isPlaying) {
                    Image(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier
							.align(Alignment.TopStart)
							.padding(4.dp)
							.size(24.dp)
                    )
                }

                // Favorite icon
                if (isFavorite) {
                    Image(
                        painter = painterResource(R.drawable.ic_heart_red),
                        contentDescription = null,
                        modifier = Modifier
							.align(Alignment.TopStart)
							.padding(4.dp)
							.size(24.dp)
                    )
                }
            }

            // Info section
            if (showInfo) {
                Column(
                    modifier = Modifier
						.fillMaxWidth()
						.padding(2.dp, vertical = 6.dp)
                ) {
                    title?.let {
                        Text(
                            text = it,
                            maxLines = if (contentText.isNullOrEmpty()) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
							color = Color.White,
							fontSize = 11.sp
                        )
                    }

                    contentText?.let {
                        Text(
                            text = it,
                            maxLines = if (title.isNullOrEmpty()) 2 else 1,
                            overflow = TextOverflow.Ellipsis,
							color = Color.Gray,
							fontSize = 11.sp
                        )
                    }

                    rating?.let {
                        Text(
							modifier = Modifier.padding(top = 4.dp),
                            text = it,
							fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    // Long click handler
    DisposableEffect(Unit) {
        onDispose {
            val activity = context.getActivity() as? Activity
            activity?.let {
                it.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU))
            }
        }
    }
}
