package com.dev.moosic.models

import android.util.Log
import com.parse.*
import org.parceler.Parcel

private const val KEY_NAME = "name"
private const val KEY_DESCRIPTION = "description"
private const val KEY_AUTHOR = "author"
private const val KEY_PLAYLIST_SONGS = "playlistSongs"
private const val KEY_SPOTIFYURI = "spotifyUri"
private const val KEY_IMAGEURI = "coverImageUri"

@Parcel
@ParseClassName("Playlist")
class Playlist() : ParseObject() {
    fun getName(): String? {
        return getString(KEY_NAME)
    }

    fun setName(name: String) {
        put(KEY_NAME, name)
    }

    fun getDescription(): String? {
        return getString(KEY_DESCRIPTION)
    }

    fun setDescription(description: String){
        put(KEY_DESCRIPTION, description)
    }

    fun getAuthor() : ParseUser? {
        return getParseUser(KEY_AUTHOR)
    }

    fun setAuthor(author: ParseUser){
        put(KEY_AUTHOR, author)
    }

    fun getSpotifyUri(): String? {
        return getString(KEY_SPOTIFYURI)
    }

    fun setSpotifyUri(uri: String) {
        put(KEY_SPOTIFYURI, uri)
    }

    fun getImageUri(): String? {
        return getString(KEY_IMAGEURI)
    }

    fun setImageUri(uri: String) {
        put(KEY_IMAGEURI, uri)
    }

    fun getPlaylistSongs() : ParseRelation<Song> {
        return getRelation(KEY_PLAYLIST_SONGS)
    }

}