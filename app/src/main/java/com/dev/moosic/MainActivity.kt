package com.dev.moosic

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
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
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.adapters.TaggedContactAdapter
import com.dev.moosic.adapters.TrackAdapter
import com.dev.moosic.controllers.FriendsController
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistController
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.fragments.*
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.localdb.daos.UserDao
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.dev.moosic.models.UserRepositorySong
import com.dev.moosic.userrepo.UserPlaylistRepository
import com.dev.moosic.viewmodels.SavedSongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.parse.ParseQuery
import com.parse.ParseRelation
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


private const val TAG = "MainActivity"
private const val TOAST_FAILED_TO_LINK_SPOTIFY_ACCOUNT
= "Failed to retrieve the linked spotify account, please quit the app and restart"
private const val TOAST_FAILED_TO_LOAD_PLAYLIST_SONGS
= "Failed to load playlist songs"
private const val TOAST_SETTING_UP_SEARCH_PAGE
= "Setting up search page.. please refresh"
private const val TOAST_SEARCH_ERROR
= "Error querying"
private const val TOAST_CONTACT_PERMISSION_WARNING
= "Permission must be granted in order to access home and friend tabs"
private const val TOAST_ALREADY_ADDED_SONG_TO_PLAYLIST
= "This song is already in your playlist"

const val WEIGHT_ADDED_SONG_TO_PLAYLIST = 2
const val WEIGHT_PLAYED_SONG = 1

const val DEFAULT_NUMBER_RECOMMENDED_USERS = 10

const val TRACK_SEEDS_LIMIT = 5
const val ARTIST_SEEDS_LIMIT = 5
const val GENRE_SEEDS_LIMIT = 5
const val DUMMY_SEED = ""
const val RECOMMENDED_SONGS_LIMIT = 20
const val SONGS_PER_FRIEND_TO_SHOW = 5
const val SIMILARITY_THRESHOLD = 0.1

const val PERMISSIONS_REQUEST_READ_CONTACTS_CODE = 100

const val DEFAULT_LOAD_ITEM_OFFSET = 0
const val DEFAULT_LOAD_NUMBER_ITEMS = 10

class MainActivity : AppCompatActivity(){
    var spotifyAppRemote : SpotifyAppRemote? = null
    val spotifyApi = SpotifyApi()

    private lateinit var mainActivitySongController: MainActivityControllerInterface
    private lateinit var mainActivityFriendsController: FriendsController

    lateinit var spotifyApiAuthToken : String
    private var currentUserId : String? = null

    private var currentTrack : Track? = null
    private var currentTrackIsPaused : Boolean? = null

    private var playerStateSubscription: Subscription<PlayerState>? = null

    private var topTracks: ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    private var topTracksDisplayed: ArrayList<Track> = ArrayList()
    private var recommendedTracks: ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    private var homeFeedItems : ArrayList<Pair<Any, String>> = ArrayList()

    private var followedFriends: ArrayList<ParseUser> = ArrayList()
    private var friendPlaylists : ArrayList<Pair<Contact, ArrayList<Song>>> = ArrayList()
    private var taggedContactList : ArrayList<Pair<Contact, String>> = ArrayList()
    private var numberFollowedFriends : Int = 0

    private var mostRecentSearchQuery: String? = null
    private var searchedTracks : ArrayList<Track> = ArrayList()

    private var parsePlaylistSongs : ArrayList<Song> = ArrayList()

    private lateinit var bottomNavigationView : BottomNavigationView
    private val fragmentManager = supportFragmentManager

    private var displayingHomeFragment = true
    private var filledFollowedFriends = false
    private var filledRecommendedSongs = false

    private var displayingFriendsFragment = false
    private var filledContacts = false

    private var displayingProfileFragment = false
    private var filledUserPlaylist = false

    private var searchMenuItem : MenuItem? = null
    private var settingsMenuItem : MenuItem? = null
    private lateinit var progressBar: ProgressBar

    private val context = this

    private var miniPlayerFragment: MiniPlayerFragment? = null
    private var miniPlayerFragmentContainer: FrameLayout? = null
    private var showMiniPlayerFragment: Boolean = false

    private var showMiniPlayerDetailFragment: Boolean = false

    private var currentContact: Contact? = null
    private var currentContactPlaylist: ArrayList<Track> = ArrayList()

    private val userPlaylistRepository = UserPlaylistRepository(this)
    private val playlistController: UserRepoPlaylistControllerInterface =
        UserRepoPlaylistController(userPlaylistRepository)

    private lateinit var db : LocalDatabase
    private lateinit var userDao: UserDao

    private val savedSongModel = SavedSongsViewModel()
    private var displayingCachedSongs = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java, LocalDbUtil.DATABASE_NAME
        ).build()

        userDao = db.userDao()

        spotifyApiAuthToken = getIntent().getExtras()?.
            getString(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN).toString()
        spotifyApi.setAccessToken(spotifyApiAuthToken)
        bottomNavigationView = findViewById(R.id.bottomNavBar)

        val toolbar = findViewById<Toolbar>(R.id.mainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            mainActivityFriendsController.exitDetailView()
        }

        bottomNavigationView.setOnItemSelectedListener { menuItem : MenuItem ->
            when (menuItem.itemId) {
                R.id.actionHome -> {
                    goToHomeFragment()
                }
                R.id.actionSearch -> {
                    goToSearchFragment()
                }
                R.id.actionProfile -> {
                    goToProfilePlaylistFragment()
                }
                R.id.actionFriends -> {
                    goToFriendsFragment() }
                else -> {}
            }
            return@setOnItemSelectedListener true
        }

        progressBar = findViewById(R.id.pbLoadingSearch)
        miniPlayerFragmentContainer = findViewById(R.id.miniPlayerFlContainer)

        Util.extractTopTenSongs(savedSongModel, db) {
            if (displayingHomeFragment) goToHomeFragment()
        }

        mainActivitySongController = MainActivitySongController()
        mainActivityFriendsController = MainActivityFriendsController()
        setUpCurrentUser()
        fetchTaggedContacts()
        setUpUserRecommendedTracks()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchMenuItem = menu?.findItem(R.id.searchMenuIcon)
        searchMenuItem?.isVisible = false
        settingsMenuItem = menu?.findItem(R.id.settingsMenuIcon)
        settingsMenuItem?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settingsMenuIcon -> { launchSettingsActivity(); return true }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val connectionParams = ConnectionParams.Builder(Util.SPOTIFY_APK_CLIENT_ID)
            .setRedirectUri(Util.SPOTIFY_APK_REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    showMiniPlayerFragment = true
                    this@MainActivity.spotifyAppRemote = spotifyAppRemote
                    connectToPlayerState()
                }
                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "error connecting to spotify app remote " + throwable.message, throwable)
                    mainActivitySongController.hideMiniPlayerPreview()
                }
            })

    }

    fun connectToPlayerState() {
        playerStateSubscription = spotifyAppRemote!!.playerApi.subscribeToPlayerState()
        playerStateSubscription?.setEventCallback { playerState: PlayerState ->
            val track: com.spotify.protocol.types.Track? = playerState.track
            if (track != null) {
                mainActivitySongController.showMiniPlayerPreview()
                val id = Util.getSpotifyIdFromUri(track.uri)
                if (track.name != currentTrack?.name && currentTrack != null) {
                    mainActivitySongController.logTrackInModel(id, WEIGHT_PLAYED_SONG)
                }
                if (track.name != currentTrack?.name) {
                    spotifyApi.service.getTrack(id, object: Callback<Track> {
                        override fun success(kaeesTrack: Track?, response: Response?) {
                            if (kaeesTrack != null && showMiniPlayerFragment) {
                                Log.e(TAG, "showing")
                                miniPlayerFragment = MiniPlayerFragment.newInstance(kaeesTrack, mainActivitySongController,
                                playerState.isPaused, playlistController)
                                fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                                miniPlayerFragment!!).commit()
                                currentTrack = kaeesTrack
                                currentTrackIsPaused = playerState.isPaused
                                if (!playerState.isPaused) {
                                    mainActivitySongController.showMiniPlayerPreview()
                                }
                            }
                        }
                        override fun failure(error: RetrofitError?) {
                            error?.message?.let { Log.e(TAG, "error getting track: " + it) }
                        }
                    })
                }
                else if (playerState.isPaused != currentTrackIsPaused) {
                    if (showMiniPlayerFragment) {
                        miniPlayerFragment = MiniPlayerFragment.newInstance(currentTrack!!, mainActivitySongController,
                            playerState.isPaused, playlistController)
                        currentTrackIsPaused = playerState.isPaused
                        fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                            miniPlayerFragment!!).commit()
                        if (!playerState.isPaused) {
                            mainActivitySongController.showMiniPlayerPreview()
                        }
                    }
                }
            }
            else {
                mainActivitySongController.hideMiniPlayerPreview()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(spotifyAppRemote);
    }

    private fun setUpCurrentUser() {
        val currentParseUser : ParseUser? = userPlaylistRepository.getUser()
        if (currentParseUser?.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME) == null){
            spotifyApi.service.getMe(object: Callback<UserPrivate> {
                override fun success(t: UserPrivate?, response: Response?) {
                    if (t != null) {
                        currentUserId = t.id
                        currentParseUser?.put(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME, t.id)
                        currentParseUser?.saveInBackground()
                        setUpUserPlaylist()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, "error getting me: " + it) }
                    Toast.makeText(this@MainActivity,
                        TOAST_FAILED_TO_LINK_SPOTIFY_ACCOUNT, Toast.LENGTH_LONG).show()
                }
            })

        } else {
            currentUserId = currentParseUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME)
            setUpUserPlaylist()
        }
    }

    private fun setUpUserPlaylist() {
        val currentParseUser = userPlaylistRepository.getUser()
        val playlistObj = currentParseUser?.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
        val playlistSongsRelation = playlistObj?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
        val query = playlistSongsRelation?.query
        query?.addDescendingOrder(Util.PARSE_KEY_CREATED_AT)
        query?.findInBackground { objects, e ->
            if (e != null) {
                Toast.makeText(context, "$TOAST_FAILED_TO_LOAD_PLAYLIST_SONGS ${e.message}",
                Toast.LENGTH_LONG).show()
            }
            else if (objects == null) {
                Toast.makeText(context, TOAST_FAILED_TO_LOAD_PLAYLIST_SONGS,
                Toast.LENGTH_LONG).show()
            }
            else {
                parsePlaylistSongs.addAll(objects)

                val userRepositorySongs = objects.map{
                    song -> val id = song.getSpotifyId();
                    val jsonStr = song.getJsonDataString();
                    if (id != null && jsonStr != null) {
                        UserRepositorySong(id, jsonStr)
                    } else {
                        null
                    }
                }.filterNotNull()

                playlistController.addAllToPlaylist(userRepositorySongs, false, false)

                val gson = Gson()
                Log.d(TAG, playlistController.getUserPlaylist().map {
                    song -> gson.fromJson(song.trackJsonData, Track::class.java).name
                }.toString())

                filledUserPlaylist = true
                if (displayingProfileFragment) goToProfilePlaylistFragment()
            }

        }
        if (displayingHomeFragment){
            goToHomeFragment()
        }
    }

    private fun setUpUserRecommendedTracks() {
        spotifyApi.service.getTopTracks(
        object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
            override fun success(
                topTracksPager: Pager<kaaes.spotify.webapi.android.models.Track>?,
                response: Response?
            ) {
                if (topTracksPager != null) {
                    topTracks.addAll(topTracksPager.items)
                    val topTracksIdList =
                        Util.extractTopTracksIds(topTracksPager.items, TRACK_SEEDS_LIMIT)
                    val topArtistsIdList =
                        Util.extractTopArtistsIds(topTracksPager.items, ARTIST_SEEDS_LIMIT)
                    val userPickedGenresString =
                        userPlaylistRepository.getUser()?.getString(Util.PARSEUSER_KEY_FAVORITE_GENRES)
                    val gson = Gson()
                    val userPickedGenresList =
                        if (userPickedGenresString == null) null else
                            gson.fromJson(
                                userPickedGenresString,
                                ArrayList::class.java
                            ) as ArrayList<String>
                    val seeds = Util.prepareRecommendationSeeds(
                        topTracksIdList,
                        topArtistsIdList,
                        userPickedGenresList
                    )
                    val recommendationQueryMap = mapOf(
                        Util.SPOTIFY_QUERY_PARAM_SEED_TRACKS to seeds.first,
                        Util.SPOTIFY_QUERY_PARAM_SEED_ARTISTS to seeds.second,
                        Util.SPOTIFY_QUERY_PARAM_SEED_GENRES to seeds.third,
                        Util.SPOTIFY_QUERY_PARAM_LIMIT to RECOMMENDED_SONGS_LIMIT
                    )
                    spotifyApi.service.getRecommendations(
                        recommendationQueryMap, object : Callback<Recommendations> {
                            override fun success(t: Recommendations?, response: Response?) {
                                if (t != null) {
                                    recommendedTracks.addAll(t.tracks)
                                    filledRecommendedSongs = true
                                    if (filledFollowedFriends && displayingHomeFragment) {
                                        displayingCachedSongs = false
                                        goToHomeFragment()
                                    }
                                }
                            }
                            override fun failure(error: RetrofitError?) {
                                error?.message?.let { Log.e(TAG, "error getting recs: " + it) }
                            }
                        }
                    )
                }
            }
            override fun failure(error: RetrofitError?) {
                error?.message?.let { Log.e(TAG, "error getting top tracks: " + it) }
            }
        })
    }

    private fun goToProfilePlaylistFragment() {
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
        displayingProfileFragment = true
        displayingFriendsFragment = false
        displayingHomeFragment = false
        supportActionBar?.setDisplayShowTitleEnabled(false)
        searchMenuItem?.collapseActionView()

        if (!filledUserPlaylist) {
            Util.showProgressBar(progressBar)
        }
        else { Util.hideProgressBar(progressBar) }
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = true
        val newFragment = ParsePlaylistFragment.newInstance(parsePlaylistSongs,
            mainActivitySongController,
            playlistController
        )
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()
    }

    private fun goToFriendsFragment() {
        displayingProfileFragment = false
        displayingFriendsFragment = true
        displayingHomeFragment = false
        supportActionBar?.setDisplayShowTitleEnabled(false)
        searchMenuItem?.collapseActionView()

        if (!filledContacts) {
            Util.showProgressBar(progressBar)
        }
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false

        val newFragment = FriendsFragment.newInstance(taggedContactList,
            mainActivityFriendsController)
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()

        if (currentContact != null) {
            supportActionBar?.setDisplayShowTitleEnabled(true)
            mainActivityFriendsController.launchDetailView(currentContact!!, false)
        }

    }

    private fun goToSearchFragment() {
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
        displayingProfileFragment = false
        displayingFriendsFragment = false
        displayingHomeFragment = false
        supportActionBar?.setDisplayShowTitleEnabled(false)

        Util.hideProgressBar(progressBar)
        searchMenuItem?.isVisible = true
        settingsMenuItem?.isVisible = false
        val searchView = (searchMenuItem?.actionView) as androidx.appcompat.widget.SearchView
        searchView.onActionViewExpanded()
        searchView.requestFocus()

        if (mostRecentSearchQuery == null){
            searchMenuItem?.expandActionView()
        } else {
            searchView.setQuery(mostRecentSearchQuery, false)
        }

        searchView.setOnQueryTextListener(object
            : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchMenuItem?.isVisible = true
                if (query != null) {
                    if (showMiniPlayerFragment) mainActivitySongController.showMiniPlayerPreview()
                    mostRecentSearchQuery = query
                    fetchQueryAndSendToFragment(query,
                        DEFAULT_LOAD_ITEM_OFFSET, DEFAULT_LOAD_NUMBER_ITEMS)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (showMiniPlayerFragment)
                    mainActivitySongController.showMiniPlayerPreview()
                return false
            }
        })
        if (currentUserId != null){
            val searchFragment = SearchFragment.newInstance(searchedTracks,
                (if (mostRecentSearchQuery == null) "" else mostRecentSearchQuery!!),
                mainActivitySongController, playlistController)
            fragmentManager.beginTransaction().replace(R.id.flContainer, searchFragment)
                .commit()

        } else {
            Toast.makeText(
                this@MainActivity, TOAST_SETTING_UP_SEARCH_PAGE,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun fetchQueryAndSendToFragment(query: String, itemOffset: Int, numberItems: Int) {
        Util.showProgressBar(progressBar)
        val queryMap = mapOf(Util.SPOTIFY_QUERY_PARAM_OFFSET to itemOffset,
            Util.SPOTIFY_QUERY_PARAM_LIMIT to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(tracksPager: TracksPager?, response: Response?) {
                if (tracksPager != null){
                    if (progressBar != null) {
                        progressBar!!.visibility = ProgressBar.INVISIBLE
                    }
                    searchedTracks.clear()
                    searchedTracks.addAll(tracksPager.tracks.items)
                    if (currentUserId != null){
                        val searchFragment = SearchFragment.newInstance(searchedTracks,
                            query, mainActivitySongController, playlistController)
                        fragmentManager.beginTransaction().replace(R.id.flContainer,
                            searchFragment)
                            .commit()
                    } else {
                        Toast.makeText(
                            this@MainActivity, TOAST_SETTING_UP_SEARCH_PAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                Util.hideProgressBar(progressBar)
            }
            override fun failure(error: RetrofitError?) {
                Toast.makeText(context, "$TOAST_SEARCH_ERROR $query: ${error?.message}",
                    Toast.LENGTH_LONG).show()
                Util.hideProgressBar(progressBar)
            }
        })
    }

    private fun loadMoreQueryTracks(query: String, itemOffset: Int, numberItems: Int,
        clearItemList: Boolean, adapter: TrackAdapter) {
        val queryMap = mapOf(Util.SPOTIFY_QUERY_PARAM_OFFSET to itemOffset,
            Util.SPOTIFY_QUERY_PARAM_LIMIT to numberItems)
        spotifyApi.service.searchTracks(query, queryMap, object: Callback<TracksPager> {
            override fun success(tracksPager: TracksPager?, response: Response?) {
                if (tracksPager != null){
                    val prevSize = searchedTracks.size
                    if (clearItemList) searchedTracks.clear()
                    searchedTracks.addAll(tracksPager.tracks.items)
                    adapter.notifyItemRangeInserted(prevSize, tracksPager.tracks.items.size)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            TOAST_SETTING_UP_SEARCH_PAGE,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            override fun failure(error: RetrofitError?) {
                Toast.makeText(context, "$TOAST_SEARCH_ERROR $query: ${error?.message}",
                    Toast.LENGTH_LONG).show()
                Util.hideProgressBar(progressBar)
            }
        })
    }

    private fun goToHomeFragment() {
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
        displayingProfileFragment = false
        displayingFriendsFragment = false
        displayingHomeFragment = true
        supportActionBar?.setDisplayShowTitleEnabled(false)
        searchMenuItem?.collapseActionView()

        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false

        if (displayingCachedSongs) {
            if (homeFeedItems.size == 0) {
                val gson = Gson()
                for (song in savedSongModel.songs){
                    val track = gson.fromJson(song.trackJsonData, Track::class.java)
                    homeFeedItems.add(Pair(track, HomeFeedItemAdapter.TAG_TRACK))
                }
            }
            val newFragment = MixedHomeFeedFragment.newInstance(homeFeedItems, topTracksDisplayed, mainActivitySongController,
                playlistController)
            fragmentManager.beginTransaction()
                .replace(R.id.flContainer, newFragment).commit()
            return
        }

        if (!filledFollowedFriends || !filledRecommendedSongs) {
            Util.showProgressBar(progressBar)
            return
        }

        if (homeFeedItems.size > 0) {
            Util.hideProgressBar(progressBar)
            val newFragment = MixedHomeFeedFragment.newInstance(homeFeedItems, topTracksDisplayed, mainActivitySongController,
                playlistController)
            fragmentManager.beginTransaction()
                .replace(R.id.flContainer, newFragment).commit()
            return
        }

        friendPlaylists.clear()
        Util.showProgressBar(progressBar)

        Util.asyncExtractFriendPlaylists(followedFriends, 0, ArrayList<Pair<Contact, ArrayList<Song>>>(),
            object:Callback<ArrayList<Pair<Contact, ArrayList<Song>>>> {
                override fun success(
                    friendPlaylistList: ArrayList<Pair<Contact, ArrayList<Song>>>?,
                    response: Response?
                ) {

                    if (friendPlaylistList != null) {
                        friendPlaylists.addAll(friendPlaylistList)

                        val mergedFriendsPlaylist = Util.getMergedFriendsPlaylist(friendPlaylists, SONGS_PER_FRIEND_TO_SHOW);
                        if (mergedFriendsPlaylist.size > 0) {
                            val playlistObject = Pair(Contact(), mergedFriendsPlaylist)
                            homeFeedItems.add(Pair(playlistObject, HomeFeedItemAdapter.TAG_FRIEND_PLAYLIST))
                        }

                        topTracksDisplayed.clear()

                        val gson = Gson()
                        savedSongModel.songs.clear()
                        var count = 0
                        for (track in recommendedTracks){
                            homeFeedItems.add(Pair(track, HomeFeedItemAdapter.TAG_TRACK))
                            if (count < 10) {
                                val userRepoSong = UserRepositorySong(track.id, gson.toJson(track))
                                savedSongModel.songs.add(userRepoSong)
                                count += 1
                            }
                        }
                        Util.saveTopTenSongs(savedSongModel, db)

                        Util.hideProgressBar(progressBar)
                        val newFragment = MixedHomeFeedFragment.newInstance(homeFeedItems, topTracksDisplayed, mainActivitySongController,
                            playlistController)
                        fragmentManager.beginTransaction()
                            .replace(R.id.flContainer, newFragment).commit()
                    }

                }
                override fun failure(error: RetrofitError?) {
                    Log.e(TAG, "error getting friend playlists: " + error?.message.toString())
                }
            })
    }

    private fun launchSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, Util.REQUEST_CODE_SETTINGS)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Util.REQUEST_CODE_SETTINGS -> {
                when (resultCode) {
                    Util.RESULT_CODE_LOG_OUT -> {
                        finish()
                    }
                    Util.RESULT_CODE_EXIT_SETTINGS -> { }
                    else -> { }
                }
            }
            else -> {}
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                asyncFillTaggedContactList(object: Callback<Unit> {
                    override fun success(t: Unit?, response: Response?) {
                        filledContacts = true
                        if (displayingFriendsFragment) {
                            goToFriendsFragment()
                        }
                        Util.hideProgressBar(progressBar)
                    }
                    override fun failure(error: RetrofitError?) {
                        error?.message?.let { Log.e(TAG, "error filling contacts list: " + it) }
                    }
                })
            } else {
                Toast.makeText(context, TOAST_CONTACT_PERMISSION_WARNING,
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        if (currentContact != null && displayingFriendsFragment) {
            mainActivityFriendsController.exitDetailView()
        }
        else { super.onBackPressed() }
    }

    private fun fetchTaggedContacts(){
        Util.showProgressBar(progressBar)
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS_CODE)
        } else {
            asyncFillTaggedContactList(object: Callback<Unit> {
                override fun success(unit: Unit?, response: Response?) {
                    filledContacts = true
                    if (displayingFriendsFragment) {
                        goToFriendsFragment()
                    }
                    Util.hideProgressBar(progressBar)
                }
                override fun failure(error: RetrofitError?) { }
            })
        }
    }

    private fun asyncFillTaggedContactList(callback: Callback<Unit>) {
        val phoneContacts = fetchContactBookContacts()
        Util.asyncFilterFriendsFromContacts(phoneContacts, userPlaylistRepository,
            object: Callback<Pair<List<Contact>, List<ParseUser>>> {
                override fun success(
                    nonFriendsAndFriends: Pair<List<Contact>, List<ParseUser>>?,
                    response: Response?
                ) {
                    if (nonFriendsAndFriends == null) {
                        callback.failure(Util.NULL_SUCCESS_ERROR)
                        return
                    }
                    val nonFriendParseUsers = nonFriendsAndFriends.first
                    val friendParseUsers = nonFriendsAndFriends.second

                    friendParseUsers?.let{followedFriends.addAll(friendParseUsers)}
                    filledFollowedFriends = true

                    if (filledRecommendedSongs && displayingHomeFragment) {
                        displayingCachedSongs = false
                        goToHomeFragment()
                    }

                    val taggedNotAddedContacts = ArrayList<Pair<Contact,String>>()
                    val usernames = ArrayList<String>()
                    for (nonFriendParseUser in nonFriendParseUsers) {
                        if (nonFriendParseUser.parseUsername != null && nonFriendParseUser.parseUsername !in usernames) {
                            taggedNotAddedContacts.add(Pair(nonFriendParseUser, Contact.KEY_NOT_FOLLOWED_CONTACT))
                            usernames.add(nonFriendParseUser.parseUsername!!)
                        }
                    }

                    for (contact in taggedNotAddedContacts) {
                        Log.e(TAG, contact.first.parseUsername + " " + contact.first.phoneNumber)
                    }

                    val taggedFollowedFriends = friendParseUsers.filterNotNull().map{
                            user -> Pair(Contact.fromParseUser(user), Contact.KEY_FOLLOWED_CONTACT)
                    }

                    val usersToIgnore = ArrayList<Contact>()
                    usersToIgnore.addAll(friendParseUsers.map{user -> Contact.fromParseUser(user)})
                    usersToIgnore.addAll(nonFriendParseUsers)

                    Util.asyncGetRecommendedFriends(false,
                        usersToIgnore, DEFAULT_NUMBER_RECOMMENDED_USERS, SIMILARITY_THRESHOLD,
                        userPlaylistRepository,
                        object: Callback<List<Pair<Contact, Double>>> {
                            override fun success(
                                recs: List<Pair<Contact, Double>>?,
                                response: Response?
                            ) {
                                if (recs == null) {
                                    callback.failure(Util.NULL_SUCCESS_ERROR)
                                    return
                                }
                                val taggedRecommendedFriends = recs.map {
                                        pair -> val contact = pair.first;
                                    contact.similarityScore = pair.second;
                                    Pair(contact, Contact.KEY_RECOMMENDED_CONTACT)
                                }

                                val pairComparator = Comparator {
                                    friend1 : Pair<Contact, String>,
                                    friend2 : Pair<Contact, String> ->
                                    friend1.first.parseUsername!!.
                                        compareTo(friend2.first.parseUsername!!,
                                            ignoreCase = true)
                                }

                                val sortedFollowedFriends
                                    = taggedFollowedFriends.sortedWith(pairComparator)
                                val sortedNotAddedContacts
                                    = taggedNotAddedContacts.sortedWith(pairComparator)
                                val sortedRecommendedFriends
                                    = taggedRecommendedFriends.sortedWith(pairComparator)

                                taggedContactList.clear()
                                taggedContactList.addAll(sortedFollowedFriends)
                                numberFollowedFriends = taggedFollowedFriends.size
                                taggedContactList.addAll(sortedNotAddedContacts)
                                taggedContactList.addAll(sortedRecommendedFriends)
                                callback.success(Unit, response)
                            }
                            override fun failure(error: RetrofitError?) {
                                callback.failure(error)
                            }
                        })
                }
                override fun failure(error: RetrofitError?) {
                    callback.failure(error)
                }
            })
    }

    private fun fetchContactBookContacts(): ArrayList<Contact> {
        val phoneContacts : ArrayList<Contact> = ArrayList()
        val resolver: ContentResolver = contentResolver
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
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone!!.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneIdx = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            val phoneNumValue = cursorPhone.getString(phoneIdx)
                            contact.name = name
                            var cleanedUpPhoneNumValue = ""
                            for (i in phoneNumValue.indices) {
                                val letter = phoneNumValue.get(i)
                                if (letter.isDigit()){
                                    cleanedUpPhoneNumValue += letter
                                }
                            }
                            contact.phoneNumber = cleanedUpPhoneNumValue
                        }
                    }
                    cursorPhone.close()
                }

                val emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null)
                if (emailCursor != null) {
                    while (emailCursor.moveToNext()) {
                        val emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        val email = emailCursor.getString(emailIdx)
                        contact.email = email
                    }
                    emailCursor.close()
                }
                phoneContacts.add(contact)
            }
        }
        cursor.close()
        return phoneContacts
    }

    inner class MainActivitySongController() : MainActivityControllerInterface {

        override fun logTrackInModel(trackId: String, weight: Int) {
            spotifyApi.service.getTrackAudioFeatures(trackId, object: Callback<AudioFeaturesTrack> {
                override fun success(t: AudioFeaturesTrack?, response: Response?) {
                    if (t != null) {
                        val featuresEntry = SongFeatures.fromAudioFeaturesTrack(t, weight)
                        featuresEntry.saveInBackground()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, "error logging track in model: " + it) }
                }
            })
        }

        override fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int,
                                          adapter: TrackAdapter) {
            loadMoreQueryTracks(query, offset, numberItemsToLoad, false, adapter)
        }

        override fun loadMoreMixedHomeFeedItems(
            trackOffset: Int,
            numberItemsToLoad: Int,
            adapter: HomeFeedItemAdapter,
            swipeContainer: SwipeRefreshLayout?
        ) {
            val queryMap = mapOf(Util.SPOTIFY_QUERY_PARAM_LIMIT to numberItemsToLoad,
                Util.SPOTIFY_QUERY_PARAM_OFFSET to trackOffset)
            spotifyApi.service.getTopTracks(
                queryMap,
                object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
                    override fun success(
                        topTracksPager: Pager<kaaes.spotify.webapi.android.models.Track>?,
                        response: Response?
                    ) {
                        if (topTracksPager != null){
                            topTracksDisplayed.addAll(topTracksPager.items)
                            val prevSize = homeFeedItems.size

                            for (track in topTracksPager.items){
                                homeFeedItems.add(Pair(track, HomeFeedItemAdapter.TAG_TRACK))
                            }

                            adapter.notifyItemRangeInserted(prevSize, topTracksPager.items.size)
                        }
                        swipeContainer?.isRefreshing = false
                    }

                    override fun failure(error: RetrofitError?) {
                        Log.e(TAG, "error getting top trackz: " + error?.message.toString())
                        swipeContainer?.isRefreshing = false
                    }
                })
        }

        override fun resetHomeFragment(swipeContainer: SwipeRefreshLayout) {
            val fragment = fragmentManager.findFragmentById(R.id.flContainer)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit()
                homeFeedItems.clear()
                goToHomeFragment()
            }
        }

        override fun playSongOnSpotify(uri: String, spotifyId: String, log: Boolean) {
            spotifyAppRemote?.playerApi?.play(uri);
            if (log) logTrackInModel(spotifyId, WEIGHT_PLAYED_SONG)
            spotifyApi.service.getTrack(spotifyId, object: Callback<Track> {
                override fun success(track: Track?, response: Response?) {
                    if (track != null) {
                        currentTrack = track
                        miniPlayerFragment = MiniPlayerFragment.newInstance(track,
                            this@MainActivitySongController, false,
                        playlistController)
                        fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                            miniPlayerFragment!!).commit()
                        showMiniPlayerPreview()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, "error fetching track to play: " + it) }
                }
            })
        }

        override fun pauseSongOnSpotify() {
            spotifyAppRemote?.playerApi?.pause()
        }

        override fun resumeSongOnSpotify() {
            spotifyAppRemote?.playerApi?.resume()
        }

        override fun goToMiniPlayerDetailView() {
            if (currentTrack != null && currentTrackIsPaused != null){
                val miniPlayerDetailFragment
                        = MiniPlayerDetailFragment.newInstance(currentTrack!!,
                    this,
                    currentTrackIsPaused!!, playlistController)
                fragmentManager.beginTransaction()
                    .setCustomAnimations(
                        androidx.appcompat.R.anim.abc_slide_in_bottom,
                        androidx.appcompat.R.anim.abc_slide_out_top
                    )
                    .add(R.id.miniPlayerDetailFlContainer,
                    miniPlayerDetailFragment).commit()
                bottomNavigationView.visibility = View.GONE
                supportActionBar?.hide()
                showMiniPlayerDetailFragment = true
            }
        }

        override fun exitMiniPlayerDetailView() {
            val miniPlayerDetailFragment
            = fragmentManager.findFragmentById(R.id.miniPlayerDetailFlContainer)
            if (miniPlayerDetailFragment != null){
                fragmentManager.beginTransaction()
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_top,
                    androidx.appcompat.R.anim.abc_slide_out_bottom)
                    .remove(miniPlayerDetailFragment).commit()
                bottomNavigationView.visibility = View.VISIBLE
                supportActionBar?.show()
                showMiniPlayerDetailFragment = false
            }
        }

        override fun showMiniPlayerPreview(){
            if (!(currentContact != null && displayingFriendsFragment)){
                val fragmentToShow = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
                if (fragmentToShow != null) {
                    fragmentManager.beginTransaction().show(fragmentToShow).commit()
                    miniPlayerFragmentContainer?.visibility = View.VISIBLE
                    showMiniPlayerFragment = true
                }
            }
        }

        override fun hideMiniPlayerPreview(pauseSong: Boolean){
            val fragmentToHide = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
            if (fragmentToHide != null) {
                fragmentManager.beginTransaction().remove(fragmentToHide).commitAllowingStateLoss()
                if (pauseSong) { pauseSongOnSpotify() }
                showMiniPlayerFragment = false
            }
            miniPlayerFragmentContainer?.visibility = View.GONE
        }
    }

    inner class MainActivityFriendsController() : FriendsController {
        private val currentUser: ParseUser? = userPlaylistRepository.getUser()
        private val currentUserFollowedRelation
        = currentUser?.getRelation<ParseUser>(Util.PARSEUSER_KEY_USERS_FOLLOWED)

        override fun followContact(contact: Contact, position: Int, adapter: TaggedContactAdapter) {
            val contactQuery = ParseQuery.getQuery(ParseUser::class.java)
            contactQuery.whereEqualTo(Contact.KEY_PHONE_NUMBER, contact.phoneNumber)
            contactQuery.findInBackground { objects, e ->
                if (e != null) {
                    e.message?.let { Log.e(TAG, "error folllowing contact: " +  it) }
                    return@findInBackground
                }
                if (objects != null) {
                    if (objects.size > 0) {
                        val userToFollow = objects.get(0)
                        currentUserFollowedRelation?.add(userToFollow)
                        currentUser?.saveInBackground{
                            if (it != null) {
                                it.message?.let { it1 -> Log.e(TAG, "error saving curr user" + it1) }
                            }
                            else {
                                taggedContactList.removeAt(position)
                                adapter.notifyItemRemoved(position)
                                val idx = getIndexForNewContact(contact.parseUsername!!,
                                    0, numberFollowedFriends)
                                taggedContactList.add(idx, Pair(contact, Contact.KEY_FOLLOWED_CONTACT))
                                adapter.notifyItemRangeChanged(idx, taggedContactList.size)
                                numberFollowedFriends += 1
                                Toast.makeText(context,
                                    "Followed " + contact.parseUsername,
                                Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        private fun getIndexForNewContact(parseUsername: String,
                                          startIndexIncl: Int,
                                          endIndexExcl: Int): Int {
            for (i in startIndexIncl until endIndexExcl) {
                val contact = taggedContactList.get(i)
                if (contact.first.parseUsername!! > parseUsername) {
                    return i
                }
            }
            return endIndexExcl
        }

        override fun unfollowContact(contact: Contact) {
            val contactQuery = ParseQuery.getQuery(ParseUser::class.java)
            contactQuery.whereEqualTo(Contact.KEY_PHONE_NUMBER, contact.phoneNumber)
            contactQuery.findInBackground { objects, e ->
                if (e != null) {
                    e.message?.let { Log.e(TAG, "error unfollowing: " + it) }
                    return@findInBackground
                }
                if (objects != null) {
                    if (objects.size > 0) {
                        val userToFollow = objects.get(0)
                        currentUserFollowedRelation?.remove(userToFollow)
                        currentUser?.saveInBackground{
                            if (it != null) {
                                Log.e(TAG, "error saving user contact relation: " + it.message)
                            }
                            else {
                                Toast.makeText(
                                    context,
                                    "Followed " + contact.parseUsername,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        override fun launchDetailView(contact: Contact, animate : Boolean) {
            Util.showProgressBar(progressBar)
            if (currentContact == null) {
                currentContact = contact
                val user = ParseUser.getQuery().get(currentContact!!.parseUserId)
                user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)?.
                getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)?.query?.
                findInBackground { songs, parseException ->
                    if (parseException != null) {
                        Toast.makeText(this@MainActivity,
                            "Error looking up ${user.username}'s songs: ${parseException.message}",
                        Toast.LENGTH_SHORT).show()
                    } else if (songs == null) {
                        Toast.makeText(this@MainActivity,
                            "Error looking up ${user.username}'s songs",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        val gson = Gson()
                        currentContactPlaylist.clear()
                        currentContactPlaylist.addAll(
                            songs.map{song -> gson.fromJson(song.getJsonDataString(), Track::class.java)}
                        )
                        goToFriendsPlaylistFragment(contact, animate)
                        Util.hideProgressBar(progressBar)
                    }
                }
            } else {
                goToFriendsPlaylistFragment(contact, animate)
                Util.hideProgressBar(progressBar)
            }

            bottomNavigationView.visibility = View.GONE
            mainActivitySongController.hideMiniPlayerPreview(false)
        }

        private fun goToFriendsPlaylistFragment(contact: Contact, animate: Boolean) {
            val newFragment = FriendPlaylistFragment.newInstance(currentContactPlaylist,
                mainActivitySongController, playlistController)
            if (animate) {
                fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.flContainer, newFragment).commit()
            }
            else {
                fragmentManager.beginTransaction().add(R.id.flContainer, newFragment).commit()
            }
            supportActionBar?.setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = "${contact.parseUsername}'s playlist"
        }

        override fun exitDetailView() {
            if (currentContact != null && displayingFriendsFragment) {
                val fragment = fragmentManager.findFragmentById(R.id.flContainer)
                if (fragment != null) {
                    fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                        .remove(fragment).commit()
                }
                currentContact = null
                supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
                supportActionBar?.setDisplayShowTitleEnabled(false)
                supportActionBar?.title = ""

                bottomNavigationView.visibility = View.VISIBLE
                mainActivitySongController.showMiniPlayerPreview()
            }
        }

    }

}

