package com.dev.moosic.controllers

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.adapters.TrackAdapter
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback

interface OldSongController {
    fun logTrackInModel(trackId: String, weight: Int) : Unit
    fun addToPlaylist(track: Track, callback: Callback<Unit>) : Unit
    fun removeFromPlaylist(track: Track, callback: Callback<Unit>)
    fun removeFromPlaylistAtIndex(track : Track, position : Int) : Unit
    fun isInPlaylist(track: Track, callback: Callback<Boolean>)

    fun loadMoreSearchTracks(query: String, offset: Int,
                             numberItemsToLoad: Int, adapter: TrackAdapter)
    fun loadMoreMixedHomeFeedItems(trackOffset: Int,
                                   numberItemsToLoad: Int, adapter: HomeFeedItemAdapter,
                                   swipeContainer: SwipeRefreshLayout?)

    fun playSongOnSpotify(uri: String, spotifyId: String)
    fun pauseSongOnSpotify()
    fun resumeSongOnSpotify()
    fun goToMiniPlayerDetailView()
    fun exitMiniPlayerDetailView()

    fun logOutFromParse()
    fun exitSettingsTab()
}