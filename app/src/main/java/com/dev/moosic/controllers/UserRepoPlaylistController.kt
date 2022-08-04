package com.dev.moosic.controllers

import android.util.Log
import com.dev.moosic.UserRepositoryInterface
import com.dev.moosic.Util
import com.dev.moosic.models.SongFeatures.Factory.TAG
import com.dev.moosic.models.UserRepositorySong

private const val TOAST_ALREADY_IN_PLAYLIST = "This song is already in your playlist"
class UserRepoPlaylistController(private val userRepository: UserRepositoryInterface)
    : UserRepoPlaylistControllerInterface{

    override fun getUserPlaylist(): ArrayList<UserRepositorySong> {
        return userRepository.getUserPlaylistSongs()
    }

    override fun logSongInModel(song: UserRepositorySong, weight: Int) {
        userRepository.logSongInModel(song, weight)
    }

    override fun isInPlaylist(songId: String): Boolean {
        return userRepository.isInUserPlaylist(songId)
    }

    override fun addToPlaylist(song: UserRepositorySong, save: Boolean, log: Boolean) {
        if (!userRepository.isInUserPlaylist(song.id)) {
            userRepository.addSongToUserPlaylist(song, save)
            if (log) logSongInModel(song, Util.ADD_TO_PLAYLIST_WEIGHT)
        } else {
            userRepository.toast(TOAST_ALREADY_IN_PLAYLIST)
        }
    }

    override fun addAllToPlaylist(songs: List<UserRepositorySong>, save: Boolean, log: Boolean) {
        for (song in songs) {
            addToPlaylist(song, save, log)
        }
    }

    override fun removeFromPlaylist(song: UserRepositorySong) {
        if (userRepository.isInUserPlaylist(song.id)){
            userRepository.removeSongFromUserPlaylist(song.id)
        }
    }

    override fun removeFromPlaylist(songId: String) {
        if (userRepository.isInUserPlaylist(songId)) {
            userRepository.removeSongFromUserPlaylist(songId)
        }
    }

}