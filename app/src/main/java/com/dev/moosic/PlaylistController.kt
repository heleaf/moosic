package com.dev.moosic

import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.models.Track

interface PlaylistController {
    fun addToPlaylist(userId : String, playlistId : String, track : Track) : Unit

    fun removeFromPlaylist(userId : String, playlistId : String, track : Track, position : Int) : Unit

    fun addToSavedTracks(trackId: String) : Unit

    fun removeFromSavedTracks(trackId: String) : Unit

    fun tracksAreSaved(tracks: List<Track>) : Array<out Boolean>?

    fun loadMoreTopSongs(offset: Int, numberItemsToLoad: Int, adapter: TopTrackAdapter)
    
//    fun loadMoreSearchResults(query: String, offset: Int, numberItemsToLoad: Int, adapter: TopTrackAdapter)
}