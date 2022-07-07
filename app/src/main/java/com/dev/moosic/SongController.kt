package com.dev.moosic

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.TrackAdapter
import kaaes.spotify.webapi.android.models.Track

interface SongController {
    fun addToPlaylist(userId: String, playlistId: String, track : Track) : Unit

    fun removeFromPlaylist(userId: String, playlistId: String, track : Track, position : Int) : Unit

    fun loadMoreTopSongs(offset: Int, numberItemsToLoad: Int, clearItems: Boolean, adapter: TrackAdapter,
                         swipeContainer: SwipeRefreshLayout)

    fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int, adapter: TrackAdapter)

    fun loadReccomendedSongs(seedArtists: String, seedGenres: String, seedTracks: String, limit: Int)

    fun playSongOnSpotify(uri: String, spotifyId: String)

    fun pauseSongOnSpotify()

    fun resumeSongOnSpotify()

    fun goToMiniPlayerDetailView()

    fun exitMiniPlayerDetailView()

    fun logOutFromParse()

    fun exitSettingsTab()
}