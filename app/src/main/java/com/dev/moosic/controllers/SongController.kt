package com.dev.moosic.controllers

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.adapters.TrackAdapter
import kaaes.spotify.webapi.android.models.Track

interface SongController {
    fun logTrackInModel(trackId: String, weight: Int) : Unit
    fun addToPlaylist(track : Track) : Unit
    fun removeFromPlaylist(track : Track, position : Int) : Unit
    fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int, adapter: TrackAdapter)
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