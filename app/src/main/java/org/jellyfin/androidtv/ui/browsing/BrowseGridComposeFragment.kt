package org.jellyfin.androidtv.ui.browsing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.request.GetItemsRequest

class BrowseGridComposeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val itemJson = requireArguments().getString(Extras.Folder)
        val item = itemJson?.let {
            Json.Default.decodeFromString(BaseItemDto.serializer(), it)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                if (item != null) {
                    BrowseGrid(title = "qwe")
                } else {
                    // TODO: Показать ошибку или экран загрузки, если элемент не был передан
                }
            }
        }
    }
}
