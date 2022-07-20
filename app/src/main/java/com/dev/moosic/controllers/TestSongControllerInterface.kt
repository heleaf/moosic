package com.dev.moosic.controllers

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface TestSongControllerInterface {
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun addToPlaylist(song: UserRepositorySong)
    fun removeFromPlaylist(song: UserRepositorySong)

    fun playSong(songId: String)
    fun pauseSong()
    fun resumeSong()
}