package com.dev.moosic

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong
import com.parse.ParseUser

interface UserRepositoryInterface {
    fun getUser(): ParseUser?
    fun getUserPlaylistSongs() : ArrayList<UserRepositorySong>
    fun addSongToUserPlaylist(song: UserRepositorySong, save: Boolean)
    fun removeSongFromUserPlaylist(songId: String)
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun isInUserPlaylist(songId: String): Boolean
    fun getSongWithId(id: String): UserRepositorySong?
    fun toast(message: String)
}