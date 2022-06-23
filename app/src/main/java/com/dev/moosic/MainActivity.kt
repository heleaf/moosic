package com.dev.moosic

import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.adapters.TopTrackAdapter
import com.dev.moosic.fragments.HomeFeedFragment
import com.dev.moosic.fragments.ProfileLibraryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.ParseUser
import com.spotify.android.appremote.api.SpotifyAppRemote

import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.*
import org.parceler.Parcel
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"

    var mSpotifyAppRemote : SpotifyAppRemote? = null
    var topTracks : ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
//    var myPlaylists : ArrayList<PlaylistSimple> = ArrayList()

    var playlistTracks : ArrayList<kaaes.spotify.webapi.android.models.PlaylistTrack> = ArrayList()

    companion object {
        val spotifyApi = SpotifyApi()
    }
    var spotifyApiAuthToken : String? = null

    var currentUserId : String? = null
    var userPlaylistId : String? = null

    var bottomNavigationView : BottomNavigationView? = null
    val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spotifyApiAuthToken = getIntent().getExtras()?.getString("accessToken")
        spotifyApi.setAccessToken(spotifyApiAuthToken)
        bottomNavigationView = findViewById(R.id.bottomNavBar)
        bottomNavigationView?.setOnItemSelectedListener { menuItem : MenuItem ->
            when (menuItem.itemId) {
                R.id.actionHome -> setUpHomeFragment()
                R.id.actionSearch -> setUpSearchFragment()
                R.id.actionProfile -> setUpProfileFragment()
                else -> {}
            }
            return@setOnItemSelectedListener true
        }
        setUpCurrentUser()
    }

    class MainActivityController() : Controller {
        val TAG = "MainActivityController"
        override fun addToPlaylist(userId: String, playlistId: String, track: Track) {
            val queryParams : Map<String, Any> = mapOf("uris" to track.uri)
            val bodyParams : Map<String, Any> = emptyMap()
            spotifyApi.service.addTracksToPlaylist(userId, playlistId, queryParams, bodyParams, object: Callback<Pager<PlaylistTrack>>{
                override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
                    Log.d(TAG, "added: " + track.name + " to playlist " + playlistId)
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "bad request: " + error?.message)
                }
            })
        }
    }

    private fun setUpCurrentUser() {
        var currentParseUser : ParseUser = ParseUser.getCurrentUser()
        if (currentParseUser.getString("userId") == null){
            spotifyApi.service.getMe(object: Callback<UserPrivate> {
                override fun success(t: UserPrivate?, response: Response?) {
                    if (t != null) {
                        currentUserId = t.id
                        currentParseUser.put("userId", t.id)
                        currentParseUser.saveInBackground()
                        setUpUserPlaylist()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    TODO("Not yet implemented")
                }
            })

        } else {
            currentUserId = currentParseUser.getString("userId")
            setUpUserPlaylist()
        }
    }

    private fun setUpUserPlaylist() {
        val currentParseUser = ParseUser.getCurrentUser()
        if (currentParseUser.getString("playlistId") == null){
            var params = mapOf("name" to "My New Playlist")
            spotifyApi.service.createPlaylist(currentUserId, params, object: Callback<Playlist> {
                override fun success(t: Playlist?, response: Response?) {
                    if (t != null) {
                        userPlaylistId = t.id
                        currentParseUser.put("playlistId", t.id)
                        currentParseUser.saveInBackground()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    if (error != null) {
                        Log.e(TAG, "error: " + error.message)
                    }
                }
            })
        } else userPlaylistId = currentParseUser.getString("playlistId")
    }

    private fun setUpProfileFragment() {
        Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show()
        val queryParams : Map<String, Any> = emptyMap()
        spotifyApi.service.getPlaylistTracks(currentUserId, userPlaylistId, queryParams,
        object: Callback<Pager<PlaylistTrack>> {
            override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
                if (t != null) {
                    Log.d(TAG, "got playlist tracks back: " + t.items.size)
                    playlistTracks.clear()
                    playlistTracks.addAll(t.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val profileFragment = ProfileLibraryFragment.newInstance(playlistTracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController()
                        )
                        fragmentManager.beginTransaction().replace(R.id.flContainer, profileFragment).commit()
                    } else {
                        Toast.makeText(this@MainActivity, "Setting up home page.. please refresh",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "error querying playlist songs: " + error?.message)
            }
        })
    }

    private fun setUpSearchFragment() {
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show()
    }

    private fun setUpHomeFragment() {
        spotifyApi.service.getTopTracks(object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
            override fun success(
                t: Pager<kaaes.spotify.webapi.android.models.Track>?,
                response: Response?
            ) {
                if (t != null){
                    Log.d(TAG, "success: " + t.toString() + " size: " + t.items.size)
                    topTracks.clear()
                    topTracks.addAll(t.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val homeFragment = HomeFeedFragment.newInstance(topTracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController()
                        )
                        fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).commit()
                    } else {
                        Toast.makeText(this@MainActivity, "Setting up home page.. please refresh",
                        Toast.LENGTH_LONG).show()
                    }
                }
                if (response != null){
                    Log.d(TAG, "success: " + response.body)
                }
            }

            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "Top tracks failure: " +  error.toString())

            }

        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }
}