package com.dev.moosic.controllers

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface TestSongControllerInterface {
    // log song in model
    fun logSongInModel(song: UserRepositorySong, weight: Int)

    // add to playlist
    fun addToPlaylist(song: UserRepositorySong)

    // remove from playlist
    fun removeFromPlaylist(song: UserRepositorySong)
}