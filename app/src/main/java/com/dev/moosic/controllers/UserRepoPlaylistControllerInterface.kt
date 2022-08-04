package com.dev.moosic.controllers

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface UserRepoPlaylistControllerInterface {
    fun getUserPlaylist(): ArrayList<UserRepositorySong>
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun isInPlaylist(songId: String) : Boolean
    fun addToPlaylist(song: UserRepositorySong, save: Boolean, log: Boolean)
    fun addAllToPlaylist(songs: List<UserRepositorySong>, save: Boolean, log: Boolean)
    fun removeFromPlaylist(song: UserRepositorySong)
    fun removeFromPlaylist(songId: String)
}