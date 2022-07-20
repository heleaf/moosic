package com.dev.moosic

import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong

interface UserRepositoryInterface {
    fun getUserPlaylistSongs() : ArrayList<UserRepositorySong>
    fun addSongToUserPlaylist(song: UserRepositorySong)
    fun removeSongFromUserPlaylist(songId: String)
    fun logSongInModel(song: UserRepositorySong, weight: Int)
    fun isInUserPlaylist(songId: String): Boolean

    fun setCurrentSong(song: UserRepositorySong)
    fun playSong(songId: String)
    fun getCurrentSong() : UserRepositorySong?
    fun getCurrentSongIsPlaying() : Boolean?
    fun pauseSong()
    fun resumeSong()

    fun connectToPlayerState()
}