package com.dev.moosic

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.TrackAdapter
import com.dev.moosic.fragments.*
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.ParseUser
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.*
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response


private const val KEY_ADD_BUTTON = "add"
private const val KEY_DELETE_BUTTON = "delete"
private const val KEY_HEART_BUTTON = "heart"
private const val KEY_LOGOUT_BUTTON = "logOut"

class MainActivity : AppCompatActivity(){
    val TAG = "MainActivity"

    val WEIGHT_ADDED_SONG_TO_PLAYLIST = 2
    val FRACTION_OF_SONG_PLAYED_THRESHOLD = 0.2f
    val WEIGHT_PLAYED_SONG = 1

    val URI_PREFIX_LENGTH = 14

    val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    val DEFAULT_ITEM_OFFSET = 0
    val DEFAULT_NUMBER_ITEMS = 10

    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"
    var mSpotifyAppRemote : SpotifyAppRemote? = null

    companion object {
        val spotifyApi = SpotifyApi()
    }

    val mainActivitySongController = MainActivitySongController()

    var spotifyApiAuthToken : String? = null
    var currentUserId : String? = null
    var userPlaylistId : String? = null

    var currentTrack : Track? = null
    var currentTrackIsPaused : Boolean? = null
    var currentTrackIsLoggedInModel : Boolean? = null

    var playerStateSubscription: Subscription<PlayerState>? = null

    var topTracks : ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()

    var contactList : ArrayList<Contact> = ArrayList()

    var mostRecentSearchQuery: String? = null
    var searchedTracks : ArrayList<Track> = ArrayList()

    var parsePlaylistSongs : ArrayList<Song> = ArrayList()

    var bottomNavigationView : BottomNavigationView? = null
    val fragmentManager = supportFragmentManager

    var searchMenuItem : MenuItem? = null
    var settingsMenuItem : MenuItem? = null
    var backMenuItem : MenuItem? = null
    var progressBar: ProgressBar? = null

    val context = this

    var miniPlayerFragment: MiniPlayerFragment? = null
    var miniPlayerFragmentContainer: FrameLayout? = null
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
                R.id.actionHome -> goToHomeFragment()
                R.id.actionSearch -> goToSearchFragment()
                R.id.actionProfile -> goToProfilePlaylistFragment()
                R.id.actionFriends -> goToFriendsFragment()
                else -> {}
            }
            return@setOnItemSelectedListener true
        }
        progressBar = findViewById(R.id.pbLoadingSearch)
        miniPlayerFragmentContainer = findViewById(R.id.miniPlayerFlContainer)
        mainActivitySongController.hideMiniPlayerPreview()
        setUpCurrentUser()
        Log.d(TAG, "fetching....")
        testFetchContacts()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchMenuItem = menu?.findItem(R.id.searchMenuIcon)
        searchMenuItem?.isVisible = false
        settingsMenuItem = menu?.findItem(R.id.settingsMenuIcon)
        settingsMenuItem?.isVisible = false
        backMenuItem = menu?.findItem(R.id.backMenuIcon)
        backMenuItem?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsMenuIcon -> { launchSettingsFragment(); return true }
            R.id.backMenuIcon -> { Log.d(TAG, "exiting settings");
                mainActivitySongController.exitSettingsTab();
                backMenuItem?.isVisible = false
                settingsMenuItem?.isVisible = true
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "starting...")

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    connectToPlayerState()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                }
            })

    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "restarting..")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "resuming...")
    }

    fun getSpotifyIdFromUri(uri: String) : String {
        if (uri.length < URI_PREFIX_LENGTH) { return "" }
        return uri.slice(IntRange(URI_PREFIX_LENGTH, uri.length - 1))
    }

    fun connectToPlayerState() {
        playerStateSubscription = mSpotifyAppRemote!!.playerApi.subscribeToPlayerState()
        playerStateSubscription?.setLifecycleCallback(object: Subscription.LifecycleCallback {
            override fun onStart() {
                Log.d(TAG, "starting player subscription?")
            }
            override fun onStop() {
                Log.d(TAG, "stopping player subscription?")
            }
        })

        playerStateSubscription?.setEventCallback { playerState: PlayerState ->
            val track: com.spotify.protocol.types.Track? = playerState.track
            if (track != null) {
                val id = getSpotifyIdFromUri(track.uri)
                // track.uri.slice(IntRange(URI_PREFIX_LENGTH, track.uri.length - 1))
                if (track.name != currentTrack?.name && currentTrack != null) {
                    mainActivitySongController.logTrackInModel(id, WEIGHT_PLAYED_SONG)
                    Log.d(TAG, "logged " + track.name + " in model")
                }
                if (track.name != currentTrack?.name) {
                    spotifyApi.service.getTrack(id, object: Callback<Track> {
                        override fun success(t: Track?, response: Response?) {
                            if (t != null) {
                                miniPlayerFragment = MiniPlayerFragment.newInstance(t, mainActivitySongController,
                                playerState.isPaused)
                                fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                                miniPlayerFragment!!).commit()
                                currentTrack = t
                                currentTrackIsPaused = playerState.isPaused
                                if (!playerState.isPaused) {
                                    mainActivitySongController.showMiniPlayerPreview()
                                }
                            }
                        }
                        override fun failure(error: RetrofitError?) {
                            Log.d(TAG, "failed to get track: " + error?.message)
                        }

                    })

                }
                else if (playerState.isPaused != currentTrackIsPaused) {
                    miniPlayerFragment = MiniPlayerFragment.newInstance(currentTrack!!, mainActivitySongController,
                        playerState.isPaused)
                    currentTrackIsPaused = playerState.isPaused
                    fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                        miniPlayerFragment!!).commit()
                    if (!playerState.isPaused) {
                       mainActivitySongController.showMiniPlayerPreview()
                    }
                }

                /*
                Log.d(TAG, "is logged in model: " + currentTrackIsLoggedInModel)
                Log.d(TAG, "threshold: " + FRACTION_OF_SONG_PLAYED_THRESHOLD + " current: " + playerState.playbackPosition.toFloat() / playerState.track.duration.toFloat())
                if (currentTrackIsLoggedInModel == false && (playerState.playbackPosition.toFloat() / playerState.track.duration.toFloat()) >= FRACTION_OF_SONG_PLAYED_THRESHOLD) {
                    // TODO: fix this so i don't add the track multiple times
                    val id = track.uri.slice(IntRange(URI_ENDPOINT_LENGTH, track.uri.length - 1))
                    MainActivityController().logTrackInModel(id, PLAYED_SONG_BEYOND_THRESHOLD_WEIGHT)
                    currentTrackIsLoggedInModel = true
                    Log.d(TAG, "logged " + track.name + " in model")
                } */
                Log.d(TAG, track.name + ": " + playerState.track.duration + " " + playerState.playbackPosition)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    inner class MainActivitySongController() : SongController{
        val TAG = "MainActivityController"
        override fun logTrackInModel(trackId: String, weight: Int) {
            spotifyApi.service.getTrackAudioFeatures(trackId, object: Callback<AudioFeaturesTrack> {
                override fun success(t: AudioFeaturesTrack?, response: Response?) {
                    if (t != null) {
                        val featuresEntry = SongFeatures.fromAudioFeaturesTrack(t, weight)
                        featuresEntry.saveInBackground()
                        Log.d(TAG, "logged " + trackId + " in model")
                    }
                }
                override fun failure(error: RetrofitError?) {
                    Log.d(TAG, "failed to get track's audio features: " + error?.message)
                }
            })
        }

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
                        Log.d(TAG, "saving " + track.name + " to parse" +
                                " playlist...")
                        Toast.makeText(this@MainActivity, "added " + track.name + " to playlist",
                        Toast.LENGTH_SHORT).show()
                    }
                }
            }
            logTrackInModel(track.id, WEIGHT_ADDED_SONG_TO_PLAYLIST)
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
            if (parsePlaylistSongs.size <= position) {
                Log.d(TAG, "position " + position + " out of bounds?")
                Log.d(TAG, "parse playlist song size: " + parsePlaylistSongs.size)
                return
            }
            val songToDelete = parsePlaylistSongs.get(position)
            parsePlaylistSongs.removeAt(position)
            playlistSongsRelation?.remove(songToDelete)
            playlist?.saveInBackground {
                if (it != null) {
                    Log.d(TAG, "error removing " + track.name + " from playlist")
                    return@saveInBackground
                }
                songToDelete.deleteInBackground()
            }
        }

        override fun removeFromPlaylist(userId: String, playlistId: String, track: Track, position : Int) {
            removeFromParsePlaylist(track, position)
        }

       fun addToSavedTracks(trackId: String) {
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

        fun removeFromSavedTracks(trackId: String) {
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
        fun tracksAreSaved(tracks: List<Track>): Array<out Boolean>? {
            val commaSeparatedIds = tracks.fold(
                ""
            ) { acc: String, track: Track ->
                Log.d(TAG, acc.isEmpty().toString() + " " + acc.isBlank().toString())
                acc
            }
            return spotifyApi.service.containsMySavedTracks(tracks[0].id);
        }

        override fun loadMoreTopSongs(offset: Int, numberItemsToLoad: Int, clearItemList: Boolean,
                                      adapter: TrackAdapter, swipeContainer: SwipeRefreshLayout) {
            loadUserTopTracks(offset, numberItemsToLoad, clearItemList, adapter, swipeContainer)
        }

        override fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int,
                                          adapter: TrackAdapter
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
            logTrackInModel(spotifyId, WEIGHT_PLAYED_SONG)
            spotifyApi.service.getTrack(spotifyId, object: Callback<Track> {
                override fun success(t: Track?, response: Response?) {
                    if (t != null) {
                        currentTrack = t
                        miniPlayerFragment = MiniPlayerFragment.newInstance(t, this@MainActivitySongController, false)
                        fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                            miniPlayerFragment!!).commit()
                        showMiniPlayerPreview()
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

        // TODO: move these to a separate controller interface?
        override fun goToMiniPlayerDetailView() {
            if (currentTrack != null && currentTrackIsPaused != null){
                val miniPlayerDetailFragment
                        = MiniPlayerDetailFragment.newInstance(currentTrack!!,
                    this,
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

        fun showMiniPlayerPreview(){
            val fragmentToShow = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
            if (fragmentToShow != null) {
                fragmentManager.beginTransaction().show(fragmentToShow).commit()
                miniPlayerFragmentContainer?.visibility = View.VISIBLE
                showMiniPlayerFragment = true
            }
        }

        fun hideMiniPlayerPreview(){
            val fragmentToHide = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
            if (fragmentToHide != null) {
                fragmentManager.beginTransaction().hide(fragmentToHide).commit()
                miniPlayerFragmentContainer?.visibility = View.GONE
                pauseSongOnSpotify()
                showMiniPlayerFragment = false
            }
        }

        override fun logOutFromParse(){
            ParseUser.logOut()
            finish()
        }

        override fun exitSettingsTab() {
            val fragment = fragmentManager.findFragmentById(R.id.flContainer)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit() // help?
            }
        }

    }

    private fun setUpCurrentUser() {
        val currentParseUser : ParseUser = ParseUser.getCurrentUser()
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
                    Log.e(TAG, "error: " + error?.message)
                    Toast.makeText(this@MainActivity,
                    "failed to retrieve the linked spotify account, please quit the app and restart",
                    Toast.LENGTH_LONG).show()
                }
            })

        } else {
            currentUserId = currentParseUser.getString("userId")
            setUpUserPlaylist()
        }
    }

    private fun setUpUserPlaylist() {
        val currentParseUser = ParseUser.getCurrentUser()
        userPlaylistId = "0" // TODO: replace this placeholder
        // get the playlist from parse
        val playlistObj = currentParseUser.getParseObject("parsePlaylist")
        val playlistSongsRelation = playlistObj?.getRelation<Song>("playlistSongs")
        val query = playlistSongsRelation?.getQuery()
        query?.addDescendingOrder("createdAt")
        val songs = query?.find()
        if (songs != null) {
            parsePlaylistSongs.addAll(songs)
        }
        goToHomeFragment()
    }

    private fun goToProfilePlaylistFragment() {
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = true
        backMenuItem?.isVisible = false
        val newFragment = ParsePlaylistFragment.newInstance(parsePlaylistSongs,
            mainActivitySongController,
            arrayListOf(KEY_DELETE_BUTTON)
        )
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()

//        val map = SongFeatures.syncGetUserPlaylistFeatureMap()
//        Log.d(TAG, map.toString())

        // testing
        SongFeatures.asyncGetUserPlaylistFeatureMap(object: Callback<Map<String, Double>> {
            override fun success(t: Map<String, Double>?, response: Response?) {
                Log.d(TAG, "onSuccess pulling interest vector: " + t.toString())
//                if (t != null) {
////                    val seedGenres = ParseUser.getCurrentUser().getString("seedGenres")
//                    val queryMap = SongFeatures.featureMapToRecommendationQueryMap(t, "", "", "")
//                    spotifyApi.service.getRecommendations(
//                        queryMap, object: Callback<Recommendations> {
//                            override fun success(t: Recommendations?, response: Response?) {
//                                Log.d(TAG, "onSuccess getting recommendations: " + t?.tracks.toString())
//                            }
//                            override fun failure(error: RetrofitError?) {
//                                Log.d(TAG, "onFailure getting recommendations: " + error?.message + " "
//                                    + error?.cause + " " + error?.localizedMessage + " "
//                                    + error?.response?.reason +
//                                    " " + ((error?.response?.body) as TypedByteArray).bytes.toString() )
//                            }
//
//                        }
//                    )
//                }
            }
            override fun failure(error: RetrofitError?) {
                Log.d(TAG, "onFailure pulling interest vector: " + error?.message)
            }
        })

    }


    private fun goToFriendsFragment() {
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false
        backMenuItem?.isVisible = false
        val newFragment = FriendsFragment.newInstance(contactList)
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()
    }


    private fun goToSearchFragment() {
        searchMenuItem?.isVisible = true
        settingsMenuItem?.isVisible = false
        backMenuItem?.isVisible = false
        val searchView = (searchMenuItem?.actionView) as androidx.appcompat.widget.SearchView
        searchView.onActionViewExpanded()
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object
            : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchMenuItem?.isVisible = true
                if (query != null) {
                    if (showMiniPlayerFragment) mainActivitySongController.showMiniPlayerPreview()
                    mostRecentSearchQuery = query
                    fetchQueryAndSendToFragment(query, DEFAULT_ITEM_OFFSET, DEFAULT_NUMBER_ITEMS)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (showMiniPlayerFragment) mainActivitySongController.showMiniPlayerPreview()
                return false
            }
        })
        if (currentUserId != null && userPlaylistId != null){
            val searchFragment = SearchFragment.newInstance(searchedTracks,
                currentUserId!!, userPlaylistId!!, mainActivitySongController,
                (if (mostRecentSearchQuery == null) "" else mostRecentSearchQuery!!)
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
        showProgressBar()
        val queryMap = mapOf("offset" to itemOffset, "limit" to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(t: TracksPager?, response: Response?) {
                Log.d(TAG, "originally successfully queried " + query + " with offset " + itemOffset
                    + " and numItems " + t?.tracks?.items?.size)
                if (t != null){
                    if (progressBar != null) {
                        progressBar!!.visibility = ProgressBar.INVISIBLE
                    }
                    searchedTracks.clear()
                    searchedTracks.addAll(t.tracks.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val searchFragment = SearchFragment.newInstance(searchedTracks,
                            currentUserId!!, userPlaylistId!!, mainActivitySongController,
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
        clearItemList: Boolean, adapter: TrackAdapter) {
        Log.d(TAG, "query: " + query + " itemOffset: " + itemOffset + " numitems: " + numberItems)
        val queryMap = mapOf("offset" to itemOffset, "limit" to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(t: TracksPager?, response: Response?) {
                Log.d(TAG, "successfully queried " + query)
                if (t != null){
                    Log.d(TAG, "loading from " + searchedTracks.size + " with " + t.tracks.items.size)
                    val prevSize = searchedTracks.size
                    if (clearItemList) searchedTracks.clear()
                    searchedTracks.addAll(t.tracks.items)
                    adapter.notifyItemRangeInserted(prevSize, t.tracks.items.size)
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

    private fun goToHomeFragment() {
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false
        backMenuItem?.isVisible = false
        showProgressBar()
        spotifyApi.service.getTopTracks(
            object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
            override fun success(
                t: Pager<kaaes.spotify.webapi.android.models.Track>?,
                response: Response?
            ) {
                hideProgressBar()
                if (t != null){
//                    Log.d(TAG, "success: " + t.toString() + " size: " + t.items.size)
                    topTracks.clear()
                    topTracks.addAll(t.items)
                    if (currentUserId != null && userPlaylistId != null){
                        val homeFragment = HomeFeedFragment.newInstance(topTracks,
                            currentUserId!!, userPlaylistId!!, mainActivitySongController
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

    private fun launchSettingsFragment() {
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false
        backMenuItem?.isVisible = true
        val settingsFragment = SettingsFragment.newInstance(mainActivitySongController)
        fragmentManager.beginTransaction().add(R.id.flContainer, settingsFragment).commit()
    }

    private fun loadUserTopTracks(itemOffset: Int, numberItems: Int,
                                  clearItemList: Boolean, adapter: TrackAdapter, swipeContainer: SwipeRefreshLayout) {
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

    private fun showProgressBar(){
        if (progressBar != null){
            progressBar!!.visibility = ProgressBar.VISIBLE
        }
    }

    private fun hideProgressBar(){
        if (progressBar != null){
            progressBar!!.visibility = ProgressBar.GONE
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
//        var builder = StringBuilder()
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
            // callback onRequestPermissionsResult
        } else {
            Log.d(TAG, "fetching contacts now...")
            getContacts()
//            val tmpText = builder.toString()
//            Log.d(TAG, "...? " + tmpText)
        }
    }

    private fun getContacts() {
//        val builder = StringBuilder()
        val resolver: ContentResolver = contentResolver;
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI, null, null, null,
            null)
        if (cursor!!.count > 0) {
            while (cursor.moveToNext()) {
                val contact = Contact()

                val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneNumberIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val id = cursor.getString(idIdx)
                val name = cursor.getString(nameIdx)
                val phoneNumber = (cursor.getString(hasPhoneNumberIdx)).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneIdx = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phoneNumValue = cursorPhone.getString(phoneIdx)
//                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
//                                phoneNumValue).append("\n\n")
                            Log.e("Name ===>",phoneNumValue);
                            Log.d(TAG, "name: " + name + " phone number: " + phoneNumValue)
                            contact.name = name
                            contact.phoneNumber = phoneNumValue
                        }
                    }
                    cursorPhone!!.close()
                }

                val emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null)
                if (emailCursor != null) {
                    while (emailCursor.moveToNext()) {
                        val emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        val email = emailCursor.getString(emailIdx)
                        Log.d(TAG, "email extracted: " + email)
                        contact.email = email
                    }
                    //contact contains all the emails of a particular contact
                    emailCursor.close()
                }

                contactList.add(contact)
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor.close()
//        return builder
    }
}

