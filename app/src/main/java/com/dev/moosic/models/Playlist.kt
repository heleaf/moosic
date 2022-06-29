package com.dev.moosic.models

import android.util.Log
import com.parse.*
import org.parceler.Parcel

@Parcel
@ParseClassName("Playlist")
class Playlist() : ParseObject() {
    final val KEY_NAME = "name"
    final val KEY_DESCRIPTION = "description"
    final val KEY_AUTHOR = "author"
    final val KEY_PLAYLIST_SONGS = "playlistSongs"
    final val KEY_SPOTIFYURI = "spotifyUri"
    final val KEY_IMAGEURI = "coverImageUri"

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

    companion object {
        val TAG = "Playlist"
        fun fromKaaesPlaylist(name: String, playlist: kaaes.spotify.webapi.android.models.Playlist) {
            var newPlaylist = Playlist()
            newPlaylist.setName(name)
            newPlaylist.setDescription(playlist.description)
            newPlaylist.setAuthor(ParseUser.getCurrentUser())
            newPlaylist.setSpotifyUri(playlist.uri)

            val playlistSongsRelation = newPlaylist.getPlaylistSongs()

            for (playlistTrack in playlist.tracks.items){
                val queryTracks : ParseQuery<Song> = ParseQuery.getQuery("Song")
                queryTracks.whereEqualTo("spotifyUri", playlistTrack.track.uri)
                val matchedSongs : List<Song>  = queryTracks.find() // already synchronous
                if (matchedSongs.size > 0) {
                    // already exists in database
                    playlistSongsRelation.add(matchedSongs[0])
                } else {
                    // add it to the database
                    val newSong = Song.fromTrack(playlistTrack.track)
                    // might need to make this asynchronous
                    newSong.saveInBackground { e ->
                        if (e != null) {
                            Log.d(TAG, "failed to save " + playlistTrack.track.name + " : " +
                                    e.message)
                        }
                    }
                    playlistSongsRelation.add(newSong)
                }
                // check if playlistTrack.track already exists in the parse database
                // if so, then just add that object in
                // otherwise initialize a new track object, save it, then add it to the relation
            }
//            newPlaylist.saveInBackground { e ->
//                if (e != null) {
//                    Log.d(TAG, "failed to save new playlist: " + e.message)
//                }
//            }
        }
    }
}