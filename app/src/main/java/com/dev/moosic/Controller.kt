package com.dev.moosic

import kaaes.spotify.webapi.android.models.Track

interface Controller {
    fun addToPlaylist(userId : String, playlistId : String, track : Track)
}