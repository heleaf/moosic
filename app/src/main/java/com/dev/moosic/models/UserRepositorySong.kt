package com.dev.moosic.models

class UserRepositorySong(id: String, trackJsonData: String) {
    val id: String
    val trackJsonData: String
    init {
        this.id = id
        this.trackJsonData = trackJsonData
    }
}