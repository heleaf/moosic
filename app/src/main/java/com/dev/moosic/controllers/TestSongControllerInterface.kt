package com.dev.moosic.controllers

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface TestSongControllerInterface {
    fun getUserPlaylist(): ArrayList<UserRepositorySong>
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun isInPlaylist(songId: String) : Boolean
    fun addToPlaylist(song: UserRepositorySong, save: Boolean)
    fun addAllToPlaylist(songs: List<UserRepositorySong>, save: Boolean)
    fun removeFromPlaylist(song: UserRepositorySong)
    fun removeFromPlaylist(songId: String)
}