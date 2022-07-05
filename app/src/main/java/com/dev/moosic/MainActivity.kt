package com.dev.moosic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.TopTrackAdapter
import com.dev.moosic.fragments.*
import com.dev.moosic.models.Song
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.ParseUser
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.*
import org.parceler.Parcels
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response


private const val KEY_ADD_BUTTON = "add"
private const val KEY_DELETE_BUTTON = "delete"
private const val KEY_HEART_BUTTON = "heart"

class MainActivity : AppCompatActivity(){
    val PERMISSIONS_REQUEST_READ_CONTACTS = 100

    val TAG = "MainActivity"
    val DEFAULT_ITEM_OFFSET = 0
    val DEFAULT_NUMBER_ITEMS = 10

    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"
    var mSpotifyAppRemote : SpotifyAppRemote? = null

    var currentTrack : Track? = null
    var currentTrackIsPaused : Boolean? = null
    var playerStateSubscription: Subscription<PlayerState>? = null

    var topTracks : ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    var searchedTracks : ArrayList<Track> = ArrayList()

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
    var progressBar: ProgressBar? = null

    val context = this

    var miniPlayerFragment: MiniPlayerFragment? = null
    var showMiniPlayerFragment: Boolean = false

    var showMiniPlayerDetailFragment: Boolean = false

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
        progressBar = findViewById(R.id.pbLoadingSearch)
        setUpCurrentUser()

        Log.d(TAG, "fetching....")
        testFetchContacts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchMenuItem = menu?.findItem(R.id.searchMenuIcon)
        searchMenuItem?.setVisible(false)

//        likedSongsMenuItem = menu?.findItem(R.id.likedSongsButton)
//        playlistMenuItem = menu?.findItem(R.id.myPlaylistButton)
//
//        likedSongsMenuItem?.setVisible(false)
//        playlistMenuItem?.setVisible(false)

        logOutMenuItem = menu?.findItem(R.id.logOutButton)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.myPlaylistButton -> { setUpUserPlaylist(); return true }
//            R.id.likedSongsButton -> { setUpLikedSongsFragment(); return true }
            R.id.logOutButton -> { ParseUser.logOut(); finish(); return true }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                    // handle errors
                }
            })

    }

    fun connected() {
        playerStateSubscription = mSpotifyAppRemote!!.playerApi.subscribeToPlayerState()
        playerStateSubscription?.setEventCallback { playerState: PlayerState ->
            val track: com.spotify.protocol.types.Track? = playerState.track
            if (track != null) {
                Log.d(TAG, track.name + ": " + playerState.track.duration + " " + playerState.playbackPosition)
                if (track.name != currentTrack?.name) {
                    val id = track.uri.slice(IntRange(14, track.uri.length - 1))
                    spotifyApi.service.getTrack(id, object: Callback<Track> {
                        override fun success(t: Track?, response: Response?) {
                            if (t != null && showMiniPlayerFragment) {
                                miniPlayerFragment = MiniPlayerFragment.newInstance(t, MainActivityController(),
                                playerState.isPaused)
                                fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                                miniPlayerFragment!!).commit()
                                currentTrack = t
                                currentTrackIsPaused = playerState.isPaused
                            }

//                            if (t != null) &&

                        }
                        override fun failure(error: RetrofitError?) {
                            Log.d(TAG, "failed to get track: " + error?.message)
                        }

                    })

                }
                else if (playerState.isPaused != currentTrackIsPaused && showMiniPlayerFragment) {
                    miniPlayerFragment = MiniPlayerFragment.newInstance(currentTrack!!, MainActivityController(),
                        playerState.isPaused)
                    currentTrackIsPaused = playerState.isPaused
                    fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                        miniPlayerFragment!!).commit()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    inner class MainActivityController() : PlaylistController {
        val TAG = "MainActivityController"

        fun addToSpotifyPlaylist(userId: String, playlistId: String, track: Track){
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

        fun addToParsePlaylist(track: Track){
            Log.d(TAG, "adding " + track.name + " to parse playlist...")
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject("parsePlaylist")
            val playlistSongsRelation = playlist?.getRelation<Song>("playlistSongs")
            val newSong = Song.fromTrack(track)
            newSong.saveInBackground {
                if (it != null) {
                    Log.d(TAG, "error saving " + track.name + " to Parse")
                    return@saveInBackground
                }
                playlistSongsRelation?.add(newSong)
                parsePlaylistSongs.add(newSong)
                playlist?.saveInBackground { e ->
                    if (e != null) Log.d(TAG, "error adding " + track.name +
                            " to parse playlist: " + e.message)
                    else {
                        Log.d(TAG, "adding " + track.name + " to spotify playlist...")
                        Toast.makeText(this@MainActivity, "added " + track.name + " to playlist",
                        Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        override fun addToPlaylist(userId: String, playlistId: String, track: Track) {
            addToParsePlaylist(track)
        }

        fun removeFromSpotifyPlaylist(userId: String, playlistId: String, track: Track, position: Int) {
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
        }

        fun removeFromParsePlaylist(track: Track, position: Int){
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject("parsePlaylist")
            val playlistSongsRelation = playlist?.getRelation<Song>("playlistSongs")
            val songToDelete = parsePlaylistSongs.get(position)
            playlistSongsRelation?.remove(songToDelete)
            playlist?.saveInBackground {
                if (it != null) {
                    Log.d(TAG, "error removing " + track.name + " from playlist")
                    return@saveInBackground
                }
//                Toast.makeText(this@MainActivity, "removed " + track.name + " from playlist",
//                    Toast.LENGTH_SHORT).show()
                songToDelete.deleteInBackground()
            }
        }

        override fun removeFromPlaylist(userId: String, playlistId: String, track: Track, position : Int) {
            removeFromParsePlaylist(track, position)
        }

        override fun addToSavedTracks(trackId: String) {
            spotifyApi.service.addToMySavedTracks(trackId, object: Callback<Any> {
                override fun success(t: Any?, response: Response?) {
                    Log.d(TAG, "added track " + trackId + " to saved tracks")
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "failed to add track " + trackId + " to saved songs: " +
                    error?.message)
                }
            });
        }

        override fun removeFromSavedTracks(trackId: String) {
            spotifyApi.service.removeFromMySavedTracks(trackId, object: Callback<Any> {
                override fun success(t: Any?, response: Response?) {
                    Log.d(TAG, "removed track " + trackId + " from saved tracks")
                }

                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "failed to remove track " + trackId + " from saved songs: " +
                            error?.message)
                }
            })
        }

        // TODO: synchronous calls don't work for me?
        override fun tracksAreSaved(tracks: List<Track>): Array<out Boolean>? {
            val commaSeparatedIds = tracks.fold(
                ""
            ) { acc: String, track: Track ->
                Log.d(TAG, acc.isEmpty().toString() + " " + acc.isBlank().toString())
                acc
            }
            return spotifyApi.service.containsMySavedTracks(tracks[0].id);
        }

        override fun loadMoreTopSongs(offset: Int, numberItemsToLoad: Int, clearItemList: Boolean,
            adapter: TopTrackAdapter, swipeContainer: SwipeRefreshLayout) {
            loadUserTopTracks(offset, numberItemsToLoad, clearItemList, adapter, swipeContainer)
        }

        override fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int,
                                          adapter: TopTrackAdapter
        ) {
             loadMoreQueryTracks(query, offset, numberItemsToLoad, false, adapter)
        }

        override fun loadReccomendedSongs(seedArtists: String, seedGenres: String, seedTracks: String,
                                          limit: Int
        ) {
            val recommendationQuery : Map<String, Any> = mapOf(
                "seed_artists" to seedArtists, "seed_genres" to seedGenres, "seed_tracks" to seedTracks
            )
            spotifyApi.service.getRecommendations(recommendationQuery, object: Callback<Recommendations> {
                override fun success(t: Recommendations?, response: Response?) {
                    if (t != null) {
                        // place t.tracks into some global list variable
                    }
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "error querying reccomendations: " + error?.message)
                }
            })
        }

        override fun playSongOnSpotify(uri: String, spotifyId: String) {
            mSpotifyAppRemote?.getPlayerApi()?.play(uri);
            spotifyApi.service.getTrack(spotifyId, object: Callback<Track> {
                override fun success(t: Track?, response: Response?) {
                    if (t != null) {
                        currentTrack = t
                        miniPlayerFragment = MiniPlayerFragment.newInstance(t, MainActivityController(), false)
                        fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                            miniPlayerFragment!!).commit()
                        showMiniPlayerFragment = true
                    }
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "error setting up mini player: " + error?.message)
                }
            })
        }

        override fun pauseSongOnSpotify() {
            mSpotifyAppRemote?.getPlayerApi()?.pause()
        }

        override fun resumeSongOnSpotify() {
            mSpotifyAppRemote?.getPlayerApi()?.resume()
        }

        override fun goToMiniPlayerDetailView() {
            if (currentTrack != null && currentTrackIsPaused != null){
                val miniPlayerDetailFragment
                        = MiniPlayerDetailFragment.newInstance(currentTrack!!,
                    MainActivityController(),
                    currentTrackIsPaused!!)
                fragmentManager.beginTransaction().replace(R.id.miniPlayerDetailFlContainer, miniPlayerDetailFragment).commit()
                bottomNavigationView?.visibility = View.GONE
                supportActionBar?.hide()
                showMiniPlayerDetailFragment = true
            }
        }

        override fun exitMiniPlayerDetailView() {
            val miniPlayerDetailFragment = fragmentManager.findFragmentById(R.id.miniPlayerDetailFlContainer)
            if (miniPlayerDetailFragment != null){
                fragmentManager.beginTransaction().remove(miniPlayerDetailFragment).commit()
                bottomNavigationView?.visibility = View.VISIBLE
                supportActionBar?.show()
                showMiniPlayerDetailFragment = false
            }
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
        userPlaylistId = "0" // placeholder
        // get the playlist from parse
        val playlistObj = currentParseUser.getParseObject("parsePlaylist")
        val playlistSongsRelation = playlistObj?.getRelation<Song>("playlistSongs")
        val query = playlistSongsRelation?.getQuery()
        query?.addDescendingOrder("createdAt")
        val songs = query?.find()
        if (songs != null) {
            parsePlaylistSongs.addAll(songs)
        }
        setUpHomeFragment()
    }

    private fun setUpLikedSongsFragment() {
        searchMenuItem?.setVisible(false)
//        likedSongsMenuItem?.setVisible(true)
//        playlistMenuItem?.setVisible(true)
        spotifyApi.service.getMySavedTracks(object: Callback<Pager<SavedTrack>> {
            override fun success(t: Pager<SavedTrack>?, response: Response?) {
                if (t != null){
                    val savedTracks = ArrayList<Track>()
                    savedTracks.addAll(t.items.map{it.track})
//                    if (currentUserId != null && userPlaylistId != null){
//                        val likedSongsFragment = PlaylistFragment.newInstance(savedTracks,
//                            currentUserId!!, userPlaylistId!!, MainActivityController(),
//                            false, false, true
//                        )
//                        fragmentManager.beginTransaction().replace(R.id.flContainer,
//                            likedSongsFragment).commit()
//                    } else {
//                        Toast.makeText(this@MainActivity, "Setting up home page.. please refresh",
//                            Toast.LENGTH_LONG).show()
//                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setUpPlaylistFragment() {
        showMiniPlayerPreview()

        searchMenuItem?.setVisible(false)
//        likedSongsMenuItem?.setVisible(true)
//        playlistMenuItem?.setVisible(true)
//        Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show()

//        val playlistObject = ParseUser.getCurrentUser().getParseObject("parsePlaylist")

        val newFragment = ParsePlaylistFragment.newInstance(parsePlaylistSongs,
            MainActivityController(), arrayListOf(KEY_DELETE_BUTTON))
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()

        Log.d(TAG, "help me" + parsePlaylistSongs.size.toString())

//        val queryParams : Map<String, Any> = emptyMap()
//        spotifyApi.service.getPlaylistTracks(currentUserId, userPlaylistId, queryParams,
//        object: Callback<Pager<PlaylistTrack>> {
//            override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
//                if (t != null) {
//                    Log.d(TAG, "got playlist tracks back: " + t.items.size)
//                    playlistTracks.clear()
//                    playlistTracks.addAll(t.items)
//                    val tracks = ArrayList<Track>()
//                    tracks.addAll(playlistTracks.map{ it.track })
//                    if (currentUserId != null && userPlaylistId != null){
//                        val profileFragment =
//                            PlaylistFragment.newInstance(tracks,
//                            currentUserId!!, userPlaylistId!!, MainActivityController(),
//                            false, true, true
//                        )
//                        fragmentManager.beginTransaction().replace(R.id.flContainer, profileFragment).commit()
//                    } else {
//                        Toast.makeText(this@MainActivity, "Setting up home page.. please refresh",
//                            Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//            override fun failure(error: RetrofitError?) {
//                Log.d(TAG, "error querying playlist songs: " + error?.message)
//            }
//        })
    }

    private fun setUpSearchFragment() {
        hideMiniPlayerPreview()
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show()
//        likedSongsMenuItem?.setVisible(false)
//        playlistMenuItem?.setVisible(false)
        searchMenuItem?.setVisible(true)
        var searchView = (searchMenuItem?.actionView) as androidx.appcompat.widget.SearchView
        searchView.onActionViewExpanded()
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object
            : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                if (query != null) {
                    showMiniPlayerPreview()
                    fetchQueryAndSendToFragment(query, DEFAULT_ITEM_OFFSET, DEFAULT_NUMBER_ITEMS)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                hideMiniPlayerPreview()
                return false
            }
        })
        searchedTracks.clear()
        if (currentUserId != null && userPlaylistId != null){
            val searchFragment = SearchFragment.newInstance(searchedTracks,
                currentUserId!!, userPlaylistId!!, MainActivityController(), ""
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

    private fun fetchQueryAndSendToFragment(query: String, itemOffset: Int, numberItems: Int) {
        // use my query to search the spotify api
        showProgressBar()
        val queryMap = mapOf("offset" to itemOffset, "limit" to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(t: TracksPager?, response: Response?) {
                Log.d(TAG, "originally successfully queried " + query + " with offset " + itemOffset
                    + " and numItems " + t?.tracks?.items?.size)
                if (t != null){
                    // send the tracks to the search fragment to display
                    if (progressBar != null) {
                        progressBar!!.visibility = ProgressBar.INVISIBLE
                    }
                    searchedTracks.clear()
                    searchedTracks.addAll(t.tracks.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val searchFragment = SearchFragment.newInstance(searchedTracks,
                            currentUserId!!, userPlaylistId!!, MainActivityController(),
                            query
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
                hideProgressBar()
            }

            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "error querying " + query + ": " + error?.message)
                hideProgressBar()
            }

        })
    }

    private fun loadMoreQueryTracks(query: String, itemOffset: Int, numberItems: Int,
        clearItemList: Boolean, adapter: TopTrackAdapter) {
        Log.d(TAG, "query: " + query + " itemOffset: " + itemOffset + " numitems: " + numberItems)
        val queryMap = mapOf("offset" to itemOffset, "limit" to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(t: TracksPager?, response: Response?) {
                Log.d(TAG, "successfully queried " + query)
                if (t != null){
//                    Log.d(TAG, "offset: " + itemOffset.toString())
                    Log.d(TAG, "loading from " + searchedTracks.size + " with " + t.tracks.items.size)
                    val prevSize = searchedTracks.size
                    if (clearItemList) searchedTracks.clear()
                    searchedTracks.addAll(t.tracks.items)
                    adapter.notifyItemRangeInserted(prevSize, t.tracks.items.size)
//                    adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Setting up search results.. please refresh",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "error querying " + query + ": " + error?.message)
                hideProgressBar()
            }
        })
    }

    private fun setUpHomeFragment() {
        showMiniPlayerPreview()

        searchMenuItem?.setVisible(false)
//        likedSongsMenuItem?.setVisible(false)
//        playlistMenuItem?.setVisible(false)
        // TODO: constants instead of magic nums
        showProgressBar()
        spotifyApi.service.getTopTracks(
            object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
            override fun success(
                t: Pager<kaaes.spotify.webapi.android.models.Track>?,
                response: Response?
            ) {
                hideProgressBar()
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
                hideProgressBar()

            }

        })

    }

    private fun loadUserTopTracks(itemOffset: Int, numberItems: Int,
        clearItemList: Boolean, adapter: TopTrackAdapter, swipeContainer: SwipeRefreshLayout) {
        val queryMap = mapOf("limit" to numberItems, "offset" to itemOffset)
        spotifyApi.service.getTopTracks(
            queryMap,
            object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
                override fun success(
                    t: Pager<kaaes.spotify.webapi.android.models.Track>?,
                    response: Response?
                ) {
                    if (t != null){
                        Log.d(TAG, "success: " + t.toString() + " size: " + t.items.size)
                        val prevSize = topTracks.size
                        if (clearItemList) topTracks.clear()
                        topTracks.addAll(t.items)
                        adapter.notifyItemRangeInserted(prevSize, t.items.size)
                    }
                    if (response != null){
                        Log.d(TAG, "success: " + response.body)
                    }
                    swipeContainer.isRefreshing = false
                }

                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "Top tracks failure: " +  error.toString())
                    swipeContainer.isRefreshing = false
                }

            })
    }

    fun showProgressBar(){
        if (progressBar != null){
            progressBar!!.visibility = ProgressBar.VISIBLE
        }
    }

    fun hideProgressBar(){
        if (progressBar != null){
            progressBar!!.visibility = ProgressBar.GONE
        }
    }

    fun showMiniPlayerPreview(){
        showMiniPlayerFragment = true
        if (fragmentManager.findFragmentById(R.id.miniPlayerFlContainer) == null
            && miniPlayerFragment != null){
            // add it in
            fragmentManager.beginTransaction()
                .add(R.id.miniPlayerFlContainer, miniPlayerFragment!!)
                .commit()
        }
    }

    fun hideMiniPlayerPreview() {
        showMiniPlayerFragment = false
        if (fragmentManager.findFragmentById(R.id.miniPlayerFlContainer) != null
            && miniPlayerFragment != null
        ) {
            fragmentManager.beginTransaction().remove(miniPlayerFragment!!).commit()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                loadContacts()
                Log.d(TAG, "permission granted?")
            } else {
                Log.d(TAG, "permission not granted?")
                //  toast("Permission must be granted in order to display contacts information")
            }
        }
    }

    fun testFetchContacts(){
        var builder = StringBuilder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
            // callback onRequestPermissionsResult
        } else {
//            builder = getContacts()
            Log.d(TAG, "hi")
        }
    }

}