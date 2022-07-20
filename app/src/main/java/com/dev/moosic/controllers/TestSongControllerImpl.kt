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

    override fun playSong(songId: String) {
        userRepository.playSong(songId)
    }

    override fun pauseSong() {
        if (userRepository.getCurrentSong() != null
            && userRepository.getCurrentSongIsPlaying() == true){
            userRepository.pauseSong()
        }
    }

    override fun resumeSong() {
        if (userRepository.getCurrentSong() != null
            && userRepository.getCurrentSongIsPlaying() == false){
            userRepository.resumeSong()
        }
    }
}