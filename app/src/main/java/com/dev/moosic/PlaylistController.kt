package com.dev.moosic

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.models.Track

interface PlaylistController {
    fun addToPlaylist(userId: String, playlistId: String, track : Track) : Unit

    fun removeFromPlaylist(userId: String, playlistId: String, track : Track, position : Int) : Unit

    fun addToSavedTracks(trackId: String) : Unit

    fun removeFromSavedTracks(trackId: String) : Unit

    fun tracksAreSaved(tracks: List<Track>) : Array<out Boolean>?

    fun loadMoreTopSongs(offset: Int, numberItemsToLoad: Int, clearItems: Boolean, adapter: TopTrackAdapter,
        swipeContainer: SwipeRefreshLayout)

    fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int, adapter: TopTrackAdapter)

    fun loadReccomendedSongs(seedArtists: String, seedGenres: String, seedTracks: String, limit: Int)

    fun playSongOnSpotify(uri: String)

    fun pauseSongOnSpotify()

    fun resumeSongOnSpotify()

}