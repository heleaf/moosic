package com.dev.moosic.controllers

import com.dev.moosic.UserRepositoryInterface
import com.dev.moosic.models.UserRepositorySong

class TestSongControllerImpl(private val userRepository: UserRepositoryInterface)
    : TestSongControllerInterface{
    override fun logSongInModel(song: UserRepositorySong, weight: Int) {
        userRepository.logSongInModel(song, weight)
    }
    override fun addToPlaylist(song: UserRepositorySong) {
        if (!userRepository.isInUserPlaylist(song.id)) {
            userRepository.addSongToUserPlaylist(song)
        }
    }
    override fun removeFromPlaylist(song: UserRepositorySong) {
        if (userRepository.isInUserPlaylist(song.id)){
            userRepository.removeSongFromUserPlaylist(song.id)
        }
    }
}