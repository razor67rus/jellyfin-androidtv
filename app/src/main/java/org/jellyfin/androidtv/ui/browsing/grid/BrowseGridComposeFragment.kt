package org.jellyfin.androidtv.ui.browsing.grid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.constant.Extras
import org.jellyfin.sdk.model.api.BaseItemDto

class BrowseGridComposeFragment : Fragment() {
    private lateinit var viewModel: BrowseGridViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val itemJson = requireArguments().getString(Extras.Folder)
        val item = itemJson?.let {
            Json.Default.decodeFromString(BaseItemDto.serializer(), it)
        } ?: throw IllegalArgumentException("Item cannot be null")

        viewModel = ViewModelProvider(
            requireActivity(),
            BrowseGridViewModelFactory(requireActivity(), item)
        )[BrowseGridViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                BrowseGrid(viewModel = viewModel)
            }
        }
    }
}
