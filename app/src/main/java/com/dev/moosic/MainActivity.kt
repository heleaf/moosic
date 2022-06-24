package com.dev.moosic

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.fragments.HomeFeedFragment
import com.dev.moosic.fragments.LikedSongsFragment
import com.dev.moosic.fragments.PlaylistFragment
import com.dev.moosic.fragments.SearchFragment
import com.dev.moosic.models.Song
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.ParseQuery
import com.parse.ParseUser
import com.spotify.android.appremote.api.SpotifyAppRemote
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.*
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
    var parsePlaylistSongs : ArrayList<Song> = ArrayList()

    companion object {
        val spotifyApi = SpotifyApi()
    }
    var spotifyApiAuthToken : String? = null

    var currentUserId : String? = null
    var userPlaylistId : String? = null

    var bottomNavigationView : BottomNavigationView? = null
    val fragmentManager = supportFragmentManager

    var searchMenuItem : MenuItem? = null
    var likedSongsMenuItem : MenuItem? = null
    var playlistMenuItem : MenuItem? = null
    var logOutMenuItem : MenuItem? = null

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
                R.id.actionProfile -> setUpPlaylistFragment()
                else -> {}
            }
            return@setOnItemSelectedListener true
        }
        setUpCurrentUser()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchMenuItem = menu?.findItem(R.id.searchMenuIcon)
        searchMenuItem?.setVisible(false)

        likedSongsMenuItem = menu?.findItem(R.id.likedSongsButton)
        playlistMenuItem = menu?.findItem(R.id.myPlaylistButton)

        likedSongsMenuItem?.setVisible(false)
        playlistMenuItem?.setVisible(false)

        logOutMenuItem = menu?.findItem(R.id.logOutButton)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.myPlaylistButton -> { setUpUserPlaylist(); return true }
            R.id.likedSongsButton -> { setUpLikedSongsFragment(); return true }
            R.id.logOutButton -> { ParseUser.logOut(); finish(); return true }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    inner class MainActivityController() : PlaylistController {
        val TAG = "MainActivityController"
        override fun addToPlaylist(userId: String, playlistId: String, track: Track) {
            // addToParsePlaylist
            // TODO: works, but parse playlist remove doesn't
            /*
            Log.d(TAG, "adding " + track.name + " to parse playlist...")
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject("parsePlaylist")
            val playlistSongsRelation = playlist?.getRelation<Song>("playlistSongs")

            val newSong = Song.fromTrack(track)
            newSong.save()
            playlistSongsRelation?.add(newSong)
            parsePlaylistSongs.add(newSong)
            playlist?.saveInBackground { e ->
                if (e != null) Log.d(TAG, "error adding " + track.name +
                        " to parse playlist: " + e.message)
                else {
                    Log.d(TAG, "adding " + track.name + " to spotify playlist...")
                }
            }
            */

            // add To SpotifyPlaylist
            val queryParams : Map<String, Any> = mapOf("uris" to track.uri)
            val bodyParams : Map<String, Any> = emptyMap()
            spotifyApi.service.addTracksToPlaylist(userId, playlistId,
                queryParams, bodyParams, object: Callback<Pager<PlaylistTrack>>{
                    override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
                        Log.d(TAG, "added: " + track.name + " to playlist " + playlistId)
                    }
                    override fun failure(error: RetrofitError?) {
                        Log.d(TAG, "bad request: " + error?.message)
                    }
                })
        }

        override fun removeFromPlaylist(userId: String, playlistId: String, track: Track, position : Int) {
            val tracksToRemove : TracksToRemove = TracksToRemove()
            val trackToRemove : TrackToRemove = TrackToRemove()
            trackToRemove.uri = track.uri
            Log.d(TAG, trackToRemove.toString())
            Log.d(TAG, tracksToRemove.toString())
            tracksToRemove.tracks = listOf(trackToRemove)

            val tracksToRemoveWithPosition : TracksToRemoveWithPosition =
                TracksToRemoveWithPosition()
            val trackToRemoveWithPosition : TrackToRemoveWithPosition =
                TrackToRemoveWithPosition()
            trackToRemoveWithPosition.uri = track.uri
            trackToRemoveWithPosition.positions = listOf(position)
            tracksToRemoveWithPosition.tracks = listOf(trackToRemoveWithPosition)

            spotifyApi.service.removeTracksFromPlaylist(userId, playlistId, tracksToRemoveWithPosition,
            object: Callback<SnapshotId> {
                override fun success(t: SnapshotId?, response: Response?) {
                    Log.d(TAG, "removed " + track.name)
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "failed to remove " + track.name + ": " + error?.message)
                }
            })

            // TODO: currently doesn't work
            /*
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject("parsePlaylist")
            if (playlist == null) Log.d(TAG, "playlist is null AAAAA")
            val playlistSongsRelation = playlist?.getRelation<Song>("playlistSongs")
            if (playlistSongsRelation == null) Log.d(TAG, "RELATION IS NULL AAAAAA")
            val songToRemove = parsePlaylistSongs[position]
            playlistSongsRelation?.remove(songToRemove)
            if (playlistSongsRelation != null) {
                playlist?.put("playlistSongs", playlistSongsRelation)
            }
            playlist?.save()
            parsePlaylistSongs.removeAt(position)
            */
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

        // get the playlist from parse
        val playlistObj = currentParseUser.getParseObject("parsePlaylist")
        val playlistSongsRelation = playlistObj?.getRelation<Song>("playlistSongs")
        val query = playlistSongsRelation?.getQuery()
        // query. // add in the order of the date? for now
        query?.addDescendingOrder("createdAt")
        val songs = query?.find()
        if (songs != null) {
            parsePlaylistSongs.addAll(songs)
        }
    }

    private fun setUpLikedSongsFragment() {
        searchMenuItem?.setVisible(false)
        likedSongsMenuItem?.setVisible(true)
        playlistMenuItem?.setVisible(true)
        spotifyApi.service.getMySavedTracks(object: Callback<Pager<SavedTrack>> {
            override fun success(t: Pager<SavedTrack>?, response: Response?) {
                if (t != null){
                    val savedTracks = ArrayList<Track>()
                    savedTracks.addAll(t.items.map{it.track})
                    if (currentUserId != null && userPlaylistId != null){
                        val likedSongsFragment = PlaylistFragment.newInstance(savedTracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController(),
                            false, false, true
                        )
                        fragmentManager.beginTransaction().replace(R.id.flContainer,
                            likedSongsFragment).commit()
                    } else {
                        Toast.makeText(this@MainActivity, "Setting up home page.. please refresh",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setUpPlaylistFragment() {
        searchMenuItem?.setVisible(false)
        likedSongsMenuItem?.setVisible(true)
        playlistMenuItem?.setVisible(true)
        Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show()
        val queryParams : Map<String, Any> = emptyMap()
        spotifyApi.service.getPlaylistTracks(currentUserId, userPlaylistId, queryParams,
        object: Callback<Pager<PlaylistTrack>> {
            override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
                if (t != null) {
                    Log.d(TAG, "got playlist tracks back: " + t.items.size)
                    playlistTracks.clear()
                    playlistTracks.addAll(t.items)
                    val tracks = ArrayList<Track>()
                    tracks.addAll(playlistTracks.map{ it.track })
                    if (currentUserId != null && userPlaylistId != null){
                        val profileFragment = PlaylistFragment.newInstance(tracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController(),
                            false, true, true
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
        likedSongsMenuItem?.setVisible(false)
        playlistMenuItem?.setVisible(false)
        searchMenuItem?.setVisible(true)

        var searchView = (searchMenuItem?.actionView) as androidx.appcompat.widget.SearchView
        searchView.onActionViewExpanded()
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object
            : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                // showProgressBar()
                if (query != null) {
                    fetchQuery(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    private fun fetchQuery(query: String) {
        // use my query to search the spotify api
        spotifyApi.service.searchTracks(query, object: Callback<TracksPager> {
            override fun success(t: TracksPager?, response: Response?) {
                Log.d(TAG, "successfully queried " + query)
                if (t != null){
                    // send the tracks to the search fragment to display
                    var searchedTracks : ArrayList<Track> = ArrayList()
                    searchedTracks.addAll(t.tracks.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val searchFragment = SearchFragment.newInstance(searchedTracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController()
                        )
                        fragmentManager.beginTransaction().replace(R.id.flContainer, searchFragment)
                            .commit()
                    } else {
                        Toast.makeText(
                            this@MainActivity, "Setting up search page.. please refresh",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
//                hideProgressBar()
            }

            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "error querying " + query + ": " + error?.message)
//                hideProgressBar()
            }

        })
    }

    private fun setUpHomeFragment() {
        searchMenuItem?.setVisible(false)
        likedSongsMenuItem?.setVisible(false)
        playlistMenuItem?.setVisible(false)
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
                        fragmentManager.beginTransaction()
                            .replace(R.id.flContainer, homeFragment).commit()
                    } else {
                        Toast.makeText(this@MainActivity,
                            "Setting up home page.. please refresh",
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

}