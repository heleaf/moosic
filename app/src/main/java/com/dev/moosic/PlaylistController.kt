package com.dev.moosic

import kaaes.spotify.webapi.android.models.Track

interface PlaylistController {
    fun addToPlaylist(userId : String, playlistId : String, track : Track) : Unit

    fun removeFromPlaylist(userId : String, playlistId : String, track : Track, position : Int) : Unit
}