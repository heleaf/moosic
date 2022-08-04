package com.dev.moosic.controllers

import android.os.Parcelable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.adapters.TrackAdapter
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcel
import retrofit.Callback

interface MainActivityControllerInterface {
    fun logTrackInModel(trackId: String, weight: Int) : Unit

    fun loadMoreSearchTracks(query: String, offset: Int,
                             numberItemsToLoad: Int, adapter: TrackAdapter)
    fun loadMoreMixedHomeFeedItems(trackOffset: Int,
                                   numberItemsToLoad: Int, adapter: HomeFeedItemAdapter,
                                   swipeContainer: SwipeRefreshLayout?)
    fun resetHomeFragment(swipeContainer: SwipeRefreshLayout)

    fun playSongOnSpotify(uri: String, spotifyId: String, log: Boolean)
    fun pauseSongOnSpotify()
    fun resumeSongOnSpotify()
    fun goToMiniPlayerDetailView()
    fun exitMiniPlayerDetailView()

    fun showMiniPlayerPreview()
    fun hideMiniPlayerPreview(pauseSong: Boolean = true)
}