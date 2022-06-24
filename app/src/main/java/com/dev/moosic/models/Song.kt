package com.dev.moosic.models

import com.parse.ParseClassName
import com.parse.ParseObject
import com.spotify.protocol.types.ListItem
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcel

@ParseClassName("Song")
class Song () : ParseObject() {
    final val KEY_SPOTIFYID = "spotifyId"
    final val KEY_NAME = "name"
    final val KEY_ARTISTS = "artists"
    final val KEY_SPOTIFYURI = "spotifyUri"
    final val KEY_IMAGEURI = "imageUri"

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

    public fun getArtists() : List<String>? {
        return getList(KEY_ARTISTS)
    }

    public fun setArtist(artist: String) {
        put(KEY_ARTISTS, artist); // does this work?
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

    companion object Factory {
        fun fromTrack(track: Track): Song {
            var song = Song()
            song.setSpotifyId(track.id)
            song.setName(track.name)
//            for (artist in track.artists) {
//                song.setArtist(artist.name) // ?
//            }
            song.setSpotifyUri(track.uri)
            song.setImageUri(if (track.album.images.size > 0)
                track.album.images.get(0).url else "")
            // TODO: replace "" with placeholder image uri
            return song // need to save in background
        }
    }

}