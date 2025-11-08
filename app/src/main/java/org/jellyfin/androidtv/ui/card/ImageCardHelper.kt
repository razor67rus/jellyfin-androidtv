package org.jellyfin.androidtv.ui.card

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.ui.itemhandling.BaseItemDtoBaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.ui.itemhandling.BaseRowType
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.sdk.model.api.BaseItemKind

class ImageCardHelper {
	companion object {

		fun getAspectRatio(item: BaseRowItem, imageType: ImageType): Float {

			var aspect = 1.0

			when (item.baseRowType) {
				BaseRowType.BaseItem -> {
					when (imageType) {
						ImageType.BANNER -> {
							aspect = ImageHelper.ASPECT_RATIO_BANNER
						}
						ImageType.THUMB -> {
							aspect = ImageHelper.ASPECT_RATIO_16_9
						}
						else -> {
							aspect = ImageHelper.ASPECT_RATIO_2_3
						}
					}

					when (item.baseItem?.type) {

						BaseItemKind.AUDIO,
						BaseItemKind.MUSIC_ALBUM,
						BaseItemKind.MUSIC_ARTIST-> aspect = 1.0

						BaseItemKind.SEASON,
						BaseItemKind.SERIES-> {
							if (imageType == ImageType.POSTER) aspect = ImageHelper.ASPECT_RATIO_2_3
						}

						BaseItemKind.EPISODE-> {
							aspect = if (item is BaseItemDtoBaseRowItem && item.preferSeriesPoster) {
								ImageHelper.ASPECT_RATIO_2_3
							} else ImageHelper.ASPECT_RATIO_16_9
						}

						BaseItemKind.COLLECTION_FOLDER,
						BaseItemKind.USER_VIEW-> {
							aspect = ImageHelper.ASPECT_RATIO_16_9
						}

						BaseItemKind.MOVIE,
						BaseItemKind.VIDEO-> {
							if (imageType == ImageType.POSTER) aspect = ImageHelper.ASPECT_RATIO_2_3
						}

						else -> {
							if (imageType == ImageType.POSTER) aspect = ImageHelper.ASPECT_RATIO_2_3
						}
					}
				}

				else -> aspect = 2.0/3.0
			}

			return aspect.toFloat()
		}

		fun getDefaultCardImage(context: Context, item: BaseRowItem): Drawable? {

			return when (item.baseRowType) {
				BaseRowType.BaseItem -> {
					when (item.baseItem?.type) {

						BaseItemKind.AUDIO,
						BaseItemKind.MUSIC_ALBUM-> {
							 ContextCompat.getDrawable(context, R.drawable.ic_album)
						}

						BaseItemKind.PERSON,
						BaseItemKind.MUSIC_ARTIST-> {
							ContextCompat.getDrawable(context, R.drawable.ic_user)
						}

						BaseItemKind.SEASON,
						BaseItemKind.SERIES,
						BaseItemKind.EPISODE-> {
							ContextCompat.getDrawable(context, R.drawable.ic_tv)
						}

						BaseItemKind.COLLECTION_FOLDER,
						BaseItemKind.USER_VIEW,
						BaseItemKind.FOLDER,
						BaseItemKind.GENRE,
						BaseItemKind.MUSIC_GENRE,
						BaseItemKind.PHOTO_ALBUM,
						BaseItemKind.PLAYLIST-> {
							ContextCompat.getDrawable(context, R.drawable.ic_folder)
						}

						BaseItemKind.PHOTO -> {
							ContextCompat.getDrawable(context, R.drawable.ic_photo)
						}

						BaseItemKind.MOVIE,
						BaseItemKind.VIDEO-> {
							ContextCompat.getDrawable(context, R.drawable.ic_clapperboard)
						}

						else -> {
							ContextCompat.getDrawable(context, R.drawable.ic_folder)
						}
					}
				}
				BaseRowType.LiveTvChannel,
				BaseRowType.LiveTvProgram,
				BaseRowType.LiveTvRecording -> {
					ContextCompat.getDrawable(context, R.drawable.ic_tv)
				}
				BaseRowType.Person -> {
					ContextCompat.getDrawable(context, R.drawable.ic_user)
				}
				BaseRowType.Chapter,
				BaseRowType.GridButton -> {
					ContextCompat.getDrawable(context, R.drawable.ic_clapperboard)
				}
				BaseRowType.SeriesTimer -> {
					ContextCompat.getDrawable(context, R.drawable.ic_tv_timer)
				}
			}
		}


	}
}
