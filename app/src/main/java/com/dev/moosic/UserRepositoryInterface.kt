package com.dev.moosic

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface UserRepositoryInterface {
    fun getUserPlaylistSongs() : ArrayList<UserRepositorySong>
    fun addSongToUserPlaylist(song: UserRepositorySong, save: Boolean)
    fun removeSongFromUserPlaylist(songId: String)
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun isInUserPlaylist(songId: String): Boolean
    fun getSongWithId(id: String): UserRepositorySong?

    fun toast(message: String)

//    fun setCurrentSong(song: UserRepositorySong)
//    fun playSong(songId: String)
//    fun getCurrentSong() : UserRepositorySong?
//    fun getCurrentSongIsPlaying() : Boolean?
//    fun pauseSong()
//    fun resumeSong()
//
//    fun connectToPlayerState()
}