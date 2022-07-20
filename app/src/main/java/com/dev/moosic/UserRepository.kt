package com.dev.moosic

//
//class UserRepository(): UserRepositoryInterface {
//    private var userPlaylistSongs : ArrayList<UserRepositorySong> = ArrayList()
//
//    override fun getUserPlaylistSongs(): ArrayList<UserRepositorySong> {
//        return userPlaylistSongs
//    }
//
//    override fun addSongToUserPlaylist(song: UserRepositorySong) {
//        val gson = Gson()
//        val track = gson.fromJson(song.trackJsonData, Track::class.java)
//        // add to parse
//    }
//
//    override fun removeSongFromUserPlaylist(song: UserRepositorySong) {
//
//    }
//
//    override fun logSongInModel(song: UserRepositorySong, weight: Int) {
//        val gson = Gson()
//        val track = gson.fromJson(song.trackJsonData, Track::class.java)
//    }
//}