package com.dev.moosic.models

import com.google.gson.Gson
import com.parse.ParseClassName
import com.parse.ParseObject
import com.spotify.protocol.types.ListItem
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcel

@ParseClassName("Song")
class Song () : ParseObject() {
    final val KEY_SPOTIFYID = "spotifyId"
    final val KEY_NAME = "name"
    final val KEY_SPOTIFYURI = "spotifyUri"
    final val KEY_IMAGEURI = "imageUri" // TODO: remove
    final val KEY_JSONDATA = "jsonData"

    public fun getSpotifyId(): String? {
        return getString(KEY_SPOTIFYID)
    }

    public fun setSpotifyId(id: String) {
        put(KEY_SPOTIFYID, id)
    }

    public fun getName(): String? {
        return getString(KEY_NAME)
    }

    public fun setName(name: String) {
        put(KEY_NAME, name)
    }

    public fun getSpotifyUri() : String? {
        return getString(KEY_SPOTIFYURI)
    }

    public fun setSpotifyUri(uri: String) {
        put(KEY_SPOTIFYURI, uri)
    }

    public fun getImageUri() : String? {
        return getString(KEY_IMAGEURI)
    }

    public fun setImageUri(uri: String) {
        put(KEY_IMAGEURI, uri)
    }

    public fun getJsonDataString() : String? {
        return getString(KEY_JSONDATA)
    }

    public fun setJsonDataString(str: String) {
        put(KEY_JSONDATA, str)
    }

    companion object Factory {
        fun fromTrack(track: Track): Song {
            var song = Song()
            song.setName(track.name)
            song.setSpotifyId(track.id)
            song.setSpotifyUri(track.uri)

            // TODO: remove this column-- do more processing in the song adapter
            // TODO: replace "" with placeholder image uri
            song.setImageUri(if (track.album.images.size > 0)
                track.album.images.get(0).url else "")

            val gson = Gson()
            val jsonData = gson.toJson(track).toString()
            song.setJsonDataString(jsonData)
            return song
        }
    }

}