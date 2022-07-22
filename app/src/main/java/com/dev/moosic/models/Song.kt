package com.dev.moosic.models

import com.google.gson.Gson
import com.parse.ParseClassName
import com.parse.ParseObject
import com.spotify.protocol.types.ListItem
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcel

@ParseClassName("Song")
class Song () : ParseObject() {
    public fun getSpotifyId(): String? {
        return getString(Factory.KEY_SPOTIFYID)
    }

    public fun setSpotifyId(id: String) {
        put(Factory.KEY_SPOTIFYID, id)
    }

    public fun getName(): String? {
        return getString(Factory.KEY_NAME)
    }

    public fun setName(name: String) {
        put(Factory.KEY_NAME, name)
    }

    public fun getSpotifyUri() : String? {
        return getString(Factory.KEY_SPOTIFYURI)
    }

    public fun setSpotifyUri(uri: String) {
        put(Factory.KEY_SPOTIFYURI, uri)
    }

    public fun getImageUri() : String? {
        return getString(Factory.KEY_IMAGEURI)
    }

    public fun setImageUri(uri: String) {
        put(Factory.KEY_IMAGEURI, uri)
    }

    public fun getJsonDataString() : String? {
        return getString(Factory.KEY_JSONDATA)
    }

    public fun setJsonDataString(str: String) {
        put(Factory.KEY_JSONDATA, str)
    }

    companion object Factory {
        const val KEY_SPOTIFYID = "spotifyId"
        const val KEY_NAME = "name"
        const val KEY_SPOTIFYURI = "spotifyUri"
        const val KEY_IMAGEURI = "imageUri"
        const val KEY_JSONDATA = "jsonData"

        fun fromTrack(track: Track): Song {
            val song = Song()
            song.setName(track.name)
            song.setSpotifyId(track.id)
            song.setSpotifyUri(track.uri)

            song.setImageUri(if (track.album.images.size > 0)
                track.album.images.get(0).url else "")

            val gson = Gson()
            val jsonData = gson.toJson(track).toString()
            song.setJsonDataString(jsonData)
            return song
        }
    }
}