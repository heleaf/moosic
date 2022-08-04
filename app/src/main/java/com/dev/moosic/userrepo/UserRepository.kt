package com.dev.moosic.userrepo

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.MainActivity
import com.dev.moosic.UserRepositoryInterface
import com.dev.moosic.Util
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.dev.moosic.models.UserRepositorySong
import com.google.gson.Gson
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

private const val TAG = "UserPlaylistRepository"

class UserPlaylistRepository(private val activity: MainActivity): UserRepositoryInterface {
    private var userPlaylistSongs : ArrayList<UserRepositorySong> = ArrayList()
    private var currentUser: ParseUser? = ParseUser.getCurrentUser()

    override fun getUser(): ParseUser? {
        return currentUser
    }

    override fun getUserPlaylistSongs(): ArrayList<UserRepositorySong> {
        return userPlaylistSongs
    }

    override fun addSongToUserPlaylist(song: UserRepositorySong, save: Boolean) {
        userPlaylistSongs.add(song)
        if (!save) return
        val gson = Gson()
        val track = gson.fromJson(song.trackJsonData, Track::class.java)
        val newSong = Song.fromTrack(track)
        newSong.saveInBackground{ songSavingException ->
            if (songSavingException != null) {
                toast("Failed to save song to playlist: ${songSavingException.message}")
                userPlaylistSongs.remove(song)
                return@saveInBackground
            }
            val user = getUser()
            val playlist = user?.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
            val playlistSongsRelation
                    = playlist?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
            playlistSongsRelation?.add(newSong)
            playlist?.saveInBackground { userPlaylistSavingException ->
                if (userPlaylistSavingException != null) {
                    toast("Failed to save playlist: ${userPlaylistSavingException.message}")
                    userPlaylistSongs.remove(song)
                } else {
                    toast("Saved ${track.name} to playlist")
                }
            }
        }
    }
    override fun removeSongFromUserPlaylist(songId: String) {
        val songToRemove = getSongWithId(songId)
        if (songToRemove != null) {
            userPlaylistSongs.remove(songToRemove)
        } else {
            toast("Error in removing, song not found in playlist")
            return
        }
        val user = getUser()
        val playlist = user?.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
        val playlistSongsRelation = playlist?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)

        playlistSongsRelation?.query?.whereEqualTo(Util.PARSESONG_KEY_SPOTIFY_ID, songId)
            ?.findInBackground { matchedSongs, error ->
                if (error != null || matchedSongs == null) {
                    toast("Failed to fetch user playlist: ${error?.message}")
                    songToRemove.let { userPlaylistSongs.add(it) }
                    return@findInBackground
                }
                if (matchedSongs.size == 0) {
                    toast("Song not found in playlist")
                    songToRemove.let { userPlaylistSongs.add(it) }
                    return@findInBackground
                }
                else {
                    val parseSongToRemove = matchedSongs.get(0)
                    playlistSongsRelation.remove(parseSongToRemove)
                    playlist.saveInBackground {
                            parseException -> if (parseException != null) {
                        toast("Failed to save playlist")
                        songToRemove.let { userPlaylistSongs.add(it) }
                        return@saveInBackground
                    }
                        toast("Removed ${parseSongToRemove.getName()} from playlist")
                        parseSongToRemove.deleteInBackground()
                    }
                }
            }
    }

    override fun logSongInModel(song: UserRepositorySong, weight: Int) {
//        Log.e(TAG, song.trackJsonData)
        val gson = Gson()
        val track = gson.fromJson(song.trackJsonData, Track::class.java)
        activity.spotifyApi.service.getTrackAudioFeatures(track.id, object:
            Callback<AudioFeaturesTrack> {
            override fun success(t: AudioFeaturesTrack?, response: Response?) {
                if (t != null) {
//                    Log.e(TAG, t.track_href)
                    val featuresEntry = SongFeatures.fromAudioFeaturesTrack(t, weight)
                    featuresEntry.saveInBackground {
                            parseException -> if (parseException != null) {
                        parseException.message?.let { Log.e(TAG, it) }
                        return@saveInBackground
                    }
                        Log.e(TAG, "logged")
                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                Log.e(TAG, error?.message.toString())
            }
        })
    }

    override fun isInUserPlaylist(songId: String): Boolean {
        val ids = userPlaylistSongs.map{song -> song.id}
        return songId in ids
    }

    override fun getSongWithId(id: String): UserRepositorySong? {
        for (song in userPlaylistSongs) {
            if (song.id == id){
                return song
            }
        }
        return null
    }

    override fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}