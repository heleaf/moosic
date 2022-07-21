package com.dev.moosic

import android.Manifest
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
import com.dev.moosic.controllers.OldSongController
import com.dev.moosic.controllers.TestSongControllerImpl
import com.dev.moosic.controllers.TestSongControllerInterface
import com.dev.moosic.fragments.*
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.dev.moosic.models.UserRepositorySong
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

const val DEFAULT_NUMBER_RECOMMENDED_USERS = 5

const val TRACK_SECTION_INCREMENT = 4
const val TRACK_SEEDS_LIMIT = 5
const val ARTIST_SEEDS_LIMIT = 5
const val GENRE_SEEDS_LIMIT = 5
const val DUMMY_SEED = ""
const val RECOMMENDED_SONGS_LIMIT = 20
const val SONGS_PER_FRIEND_TO_SHOW = 5

const val URI_PREFIX_LENGTH = 14
const val PERMISSIONS_REQUEST_READ_CONTACTS_CODE = 100

const val DEFAULT_LOAD_ITEM_OFFSET = 0
const val DEFAULT_LOAD_NUMBER_ITEMS = 10

class MainActivity : AppCompatActivity(){
    var spotifyAppRemote : SpotifyAppRemote? = null

    val spotifyApi = SpotifyApi()

    val mainActivitySongController = MainActivitySongController()
    private val mainActivityFriendsController = MainActivityFriendsController()

    lateinit var spotifyApiAuthToken : String
    var currentUserId : String? = null

    var currentTrack : Track? = null
    var currentTrackIsPaused : Boolean? = null

    var playerStateSubscription: Subscription<PlayerState>? = null

    var topTracks: ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    var topTracksDisplayed: ArrayList<Track> = ArrayList()
    var recommendedTracks: ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    var homeFeedItems : ArrayList<Pair<Any, String>> = ArrayList()

    var followedFriends: ArrayList<ParseUser> = ArrayList()
    var friendPlaylists : ArrayList<Pair<Contact, ArrayList<Song>>> = ArrayList()
    var taggedContactList : ArrayList<Pair<Contact, String>> = ArrayList()
    var numberFollowedFriends : Int = 0

    var mostRecentSearchQuery: String? = null
    var searchedTracks : ArrayList<Track> = ArrayList()

    var parsePlaylistSongs : ArrayList<Song> = ArrayList()

    lateinit var bottomNavigationView : BottomNavigationView
    val fragmentManager = supportFragmentManager

    var displayingHomeFragment = true
    var filledFollowedFriends = false
    var filledRecommendedSongs = false

    var displayingFriendsFragment = false
    var filledContacts = false

    var displayingProfileFragment = false
    var filledUserPlaylist = false

    var searchMenuItem : MenuItem? = null
    var settingsMenuItem : MenuItem? = null
    lateinit var progressBar: ProgressBar

    val context = this

    var miniPlayerFragment: MiniPlayerFragment? = null
    var miniPlayerFragmentContainer: FrameLayout? = null
    var showMiniPlayerFragment: Boolean = false

    var showMiniPlayerDetailFragment: Boolean = false

    var currentContact: Contact? = null
    var currentContactPlaylist: ArrayList<Track> = ArrayList()

    val testSongController: TestSongControllerInterface =
        TestSongControllerImpl(UserRepository())
    // TODO: update this so that the user repository's parse playlist
    // is synced with the global one i'm currently using

    lateinit var db : LocalDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java, LocalDbUtil.DATABASE_NAME
        ).build()

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
                    supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
                    displayingProfileFragment = false
                    displayingFriendsFragment = false
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    searchMenuItem?.collapseActionView()
                    goToHomeFragment()
                }
                R.id.actionSearch -> {
                    supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
                    displayingProfileFragment = false
                    displayingFriendsFragment = false
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    goToSearchFragment()
                }
                R.id.actionProfile -> {
                    supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
                    displayingProfileFragment = true
                    displayingFriendsFragment = false
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    searchMenuItem?.collapseActionView()
                    goToProfilePlaylistFragment()
                }
                R.id.actionFriends -> {
                    displayingProfileFragment = false
                    displayingFriendsFragment = true
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    searchMenuItem?.collapseActionView()
                    goToFriendsFragment() }
                else -> {}
            }
            return@setOnItemSelectedListener true
        }

        progressBar = findViewById(R.id.pbLoadingSearch)
        miniPlayerFragmentContainer = findViewById(R.id.miniPlayerFlContainer)
        setUpCurrentUser()
        fetchContacts()
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
                    this@MainActivity.spotifyAppRemote = spotifyAppRemote
                    connectToPlayerState()
                }
                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, throwable.message, throwable)
                    mainActivitySongController.hideMiniPlayerPreview()
                }
            })

    }

    // add this to userRepository / controller ?
    fun connectToPlayerState() {
        playerStateSubscription = spotifyAppRemote!!.playerApi.subscribeToPlayerState()
        playerStateSubscription?.setEventCallback { playerState: PlayerState ->
            val track: com.spotify.protocol.types.Track? = playerState.track
            if (track != null) {
                val id = Util.getSpotifyIdFromUri(track.uri)
                if (track.name != currentTrack?.name && currentTrack != null) {
                    mainActivitySongController.logTrackInModel(id, WEIGHT_PLAYED_SONG)
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
                            error?.message?.let { Log.e(TAG, it) }
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
        val currentParseUser : ParseUser = ParseUser.getCurrentUser()
        if (currentParseUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME) == null){
            spotifyApi.service.getMe(object: Callback<UserPrivate> {
                override fun success(t: UserPrivate?, response: Response?) {
                    if (t != null) {
                        currentUserId = t.id
                        currentParseUser.put(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME, t.id)
                        currentParseUser.saveInBackground()
                        setUpUserPlaylist()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, it) }
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
        val currentParseUser = ParseUser.getCurrentUser()
        val playlistObj = currentParseUser.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
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
                filledUserPlaylist = true
                if (displayingProfileFragment) goToProfilePlaylistFragment()
            }
        }
        goToHomeFragment()
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
                        extractTopTracksIds(topTracksPager.items, TRACK_SEEDS_LIMIT)
                    val topArtistsIdList =
                        extractTopArtistsIds(topTracksPager.items, ARTIST_SEEDS_LIMIT)
                    val userPickedGenresString =
                        ParseUser.getCurrentUser().getString(Util.PARSEUSER_KEY_FAVORITE_GENRES)
                    val gson = Gson()
                    val userPickedGenresList =
                        if (userPickedGenresString == null) null else
                            gson.fromJson(
                                userPickedGenresString,
                                ArrayList::class.java
                            ) as ArrayList<String>
                    val seeds = prepareRecommendationSeeds(
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
                                        goToHomeFragment()
                                    }
                                }
                            }
                            override fun failure(error: RetrofitError?) {
                                error?.message?.let { Log.e(TAG, it) }
                            }
                        }
                    )
                }
            }
            override fun failure(error: RetrofitError?) {
                error?.message?.let { Log.e(TAG, it) }
            }
        })
    }

    private fun goToProfilePlaylistFragment() {
        if (!filledUserPlaylist) {
            showProgressBar()
        }
        else { hideProgressBar() }
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = true
        val newFragment = ParsePlaylistFragment.newInstance(parsePlaylistSongs,
            mainActivitySongController,
            arrayListOf(Util.FLAG_DELETE_BUTTON)
        )
        fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit()
    }

    private fun goToFriendsFragment() {
        if (!filledContacts) {
            showProgressBar()
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
        hideProgressBar()
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
                mainActivitySongController)
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
        showProgressBar()
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
                            query, mainActivitySongController )
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
                hideProgressBar()
            }
            override fun failure(error: RetrofitError?) {
                Toast.makeText(context, "$TOAST_SEARCH_ERROR $query: ${error?.message}",
                    Toast.LENGTH_LONG).show()
                hideProgressBar()
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
                hideProgressBar()
            }
        })
    }

    private fun goToHomeFragment() {
        searchMenuItem?.isVisible = false
        settingsMenuItem?.isVisible = false

        if (!filledFollowedFriends || !filledRecommendedSongs) {
            showProgressBar()
            return
        }

        homeFeedItems.clear()
        friendPlaylists.clear()
        showProgressBar()

        asyncExtractFriendPlaylists(followedFriends, 0, ArrayList<Pair<Contact, ArrayList<Song>>>(),
            object:Callback<ArrayList<Pair<Contact, ArrayList<Song>>>> {
                override fun success(
                    friendPlaylistList: ArrayList<Pair<Contact, ArrayList<Song>>>?,
                    response: Response?
                ) {

                    if (friendPlaylistList != null) {
                        friendPlaylists.addAll(friendPlaylistList)

                        val mergedFriendsPlaylist = getMergedFriendsPlaylist(SONGS_PER_FRIEND_TO_SHOW);
                        if (mergedFriendsPlaylist.size > 0) {
                            val playlistObject = Pair(Contact(), mergedFriendsPlaylist)
                            homeFeedItems.add(Pair(playlistObject, HomeFeedItemAdapter.TAG_FRIEND_PLAYLIST))
                        }

                        topTracksDisplayed.clear()
                        for (track in recommendedTracks){
                            homeFeedItems.add(Pair(track, HomeFeedItemAdapter.TAG_TRACK))
                        }

                        hideProgressBar()
                        val newFragment = MixedHomeFeedFragment.newInstance(homeFeedItems, topTracksDisplayed, mainActivitySongController,
                        testSongController)
                        fragmentManager.beginTransaction()
                            .replace(R.id.flContainer, newFragment).commit()
                    }

                }
                override fun failure(error: RetrofitError?) {
                    Log.e(TAG, error?.message.toString())
                }
            })
    }

    fun getMergedFriendsPlaylist(songsPerFriend : Int?): ArrayList<Song> {
        val mergedList = ArrayList<Song>()
        for (playlist in friendPlaylists){
            val songList = playlist.second
            if (songsPerFriend != null) {
                var songListIdx = 0
                while (songListIdx < songList.size && songListIdx < songsPerFriend) {
                    val currSong = songList.get(songListIdx)
                    if (currSong.getSpotifyId() !in mergedList.map{song -> song.getSpotifyId()}){
                        mergedList.add(currSong)
                    }
                    songListIdx += 1
                }
            } else {
                mergedList.addAll(songList)
            }
        }
        return mergedList
    }

    private fun prepareRecommendationSeeds(topTracksIdList: List<String>,
                                           topArtistsIdList: List<String>,
                                           userPickedGenresList: ArrayList<String>?)
    : Triple<String, String, String> {
        try {
            if (topTracksIdList.isEmpty() && topArtistsIdList.isEmpty()) {
                val slicedList = if (userPickedGenresList?.size!! > GENRE_SEEDS_LIMIT)
                    userPickedGenresList.slice(IntRange(0, GENRE_SEEDS_LIMIT - 1))
                    else userPickedGenresList
                val genreSeed =
                    slicedList.fold(
                        ""
                    ){ accumulatedString, genre ->
                        if (accumulatedString == "") genre else "$accumulatedString,$genre"
                    }
                return Triple(DUMMY_SEED, DUMMY_SEED,genreSeed)
            }
            else if (userPickedGenresList == null || userPickedGenresList.size == 0) {
                val trackSeed = topTracksIdList.slice(IntRange(0,2)).fold(
                    ""
                ) { accumulatedString, trackId ->
                    if (accumulatedString == "") trackId else "$accumulatedString,$trackId"
                }
                val artistSeed =
                    topArtistsIdList.slice(IntRange(0,1)).fold(
                        ""
                    ){ accumulatedString, artistId ->
                        if (accumulatedString == "") artistId else "$accumulatedString,$artistId"
                    }
                return Triple(trackSeed, artistSeed, DUMMY_SEED)
            }
            else {
                val trackSeed = topTracksIdList.slice(IntRange(0,1)).fold(
                    ""
                ) { accumulatedString, trackId ->
                    if (accumulatedString == "") trackId else "$accumulatedString,$trackId"
                }
                val artistSeed = topArtistsIdList.slice(IntRange(0,1)).fold(
                    ""
                ){ accumulatedString, artistId ->
                    if (accumulatedString == "") artistId else "$accumulatedString,$artistId"
                }
                val genreSeed = userPickedGenresList.get(0)
                return Triple(trackSeed, artistSeed, genreSeed)
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(TAG, it) }
            return Triple(DUMMY_SEED, DUMMY_SEED, DUMMY_SEED)
        }
    }

    private fun extractTopTracksIds(tracks: List<Track>, limit: Int): List<String> {
        val topTrackIds = ArrayList<String>()
        var trackIdx = 0
        while (topTrackIds.size < limit && trackIdx < tracks.size) {
            val track = tracks.get(trackIdx)
            topTrackIds.add(track.id)
            trackIdx += 1
        }
        return topTrackIds
    }

    private fun extractTopArtistsIds(tracks: List<Track>, limit: Int): List<String> {
        val topArtistIds = ArrayList<String>()
        var trackIdx = 0
        while (topArtistIds.size < limit && trackIdx < tracks.size) {
            val track = tracks.get(trackIdx)
            val artists = track.artists
            var artistIdx = 0
            while (topArtistIds.size < limit && artistIdx < artists.size) {
                val artist = artists.get(artistIdx)
                if (artist.id !in topArtistIds) {
                    topArtistIds.add(artist.id)
                }
                artistIdx += 1
            }
            trackIdx += 1
        }
        return topArtistIds
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
                    Util.RESULT_CODE_EXIT_SETTINGS -> {
                        // do nothing?
                    }
                    else -> { }
                }
            }
            else -> {}
        }
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
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                asyncFillTaggedContactList(object: Callback<Unit> {
                    override fun success(t: Unit?, response: Response?) {
                        filledContacts = true
                        if (displayingFriendsFragment) {
                            goToFriendsFragment()
                        }
                        hideProgressBar()
                    }
                    override fun failure(error: RetrofitError?) {
                        error?.message?.let { Log.e(TAG, it) }
                    }
                })
            } else {
                Toast.makeText(context, TOAST_CONTACT_PERMISSION_WARNING,
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchContacts(){
        showProgressBar()
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
                    hideProgressBar()
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, it) }
                }
            })
        }
    }

    private fun asyncFillTaggedContactList(callback: Callback<Unit>) {
        val phoneContacts = getContacts()
        asyncFilterFriendsFromContacts(phoneContacts,
            object: Callback<Pair<List<Contact>, List<Contact>>> {
                override fun success(nonFriendsAndFriends: Pair<List<Contact>, List<Contact>>?, response: Response?) {
                    if (nonFriendsAndFriends == null) {
                        callback.failure(Util.NULL_SUCCESS_ERROR)
                        return
                    }
                    val nonFriendParseUsers = nonFriendsAndFriends.first
                    val friendParseUsers = nonFriendsAndFriends.second

                    val taggedNotAddedContacts = nonFriendParseUsers.map{
                            contact -> Pair(contact, Contact.KEY_NOT_FOLLOWED_CONTACT)
                    }

                    val taggedFollowedFriends = friendParseUsers.map{
                            contact -> Pair(contact, Contact.KEY_FOLLOWED_CONTACT)
                    }

                    asyncGetRecommendedFriends(false,
                        friendParseUsers, DEFAULT_NUMBER_RECOMMENDED_USERS,
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

    private fun getContacts(): ArrayList<Contact> {
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

    fun asyncGetRecommendedFriends(includeCurrentUser: Boolean,
                                   contactsToIgnore: List<Contact>,
                                   numberOfUsersRequested: Int,
                                   callback: Callback<List<Pair<Contact, Double>>>) {
        val userQuery = ParseUser.getQuery()
        val usernamesToIgnore = ArrayList<String>()
        if (!includeCurrentUser) {
            usernamesToIgnore.add(ParseUser.getCurrentUser().username)
        }
        for (contact in contactsToIgnore) {
            contact.parseUsername?.let { usernamesToIgnore.add(it) }
        }
        userQuery.whereNotContainedIn(Util.PARSEUSER_KEY_USERNAME, usernamesToIgnore)
        userQuery.findInBackground { objects, e ->
            if (e != null) {
                val throwable = Throwable(e.message.toString())
                val error = retrofit.RetrofitError.unexpectedError(
                    Util.DUMMY_URL, throwable
                )
                callback.failure(error)
            }
            else if (objects == null) {
                callback.failure(Util.NULL_SUCCESS_ERROR)
            }
            asyncGetInterestVectorOfUserList(objects!!, 0,
                ArrayList<Pair<Contact, Map<String, Double>>>(),
                object: Callback<List<Pair<Contact, Map<String, Double>>>> {
                override fun success(
                    interestVectors: List<Pair<Contact, Map<String, Double>>>?,
                    response: Response?
                ) {
                    if (interestVectors == null) {
                        callback.failure(Util.NULL_SUCCESS_ERROR)
                        return
                    }
                    SongFeatures.asyncGetUserPlaylistFeatureMap(ParseUser.getCurrentUser(),
                        object: Callback<Map<String, Double>> {
                        override fun success(userInterestVector: Map<String, Double>?, response: Response?) {
                            if (userInterestVector == null) {
                                callback.failure(Util.NULL_SUCCESS_ERROR)
                                return
                            }
                            val interestVectorSimilarities : List<Pair<Contact, Double>>
                                    = interestVectors.map {
                                    userVectorPair ->
                                Pair(userVectorPair.first,
                                    SongFeatures.computeVectorSimilarityScore(
                                        userVectorPair.second,
                                        userInterestVector))
                            }
                            val similaritiesFilteredOutNaNs = interestVectorSimilarities.filter {
                                    it -> !it.second.isNaN()
                            }
                            val vectorComparator = Comparator {
                                    vec1 : Pair<Contact, Double>,
                                    vec2 : Pair<Contact, Double> ->
                                if (vec1.second - vec2.second > 0.0) 1
                                else if (vec1.second - vec2.second == 0.0) 0
                                else -1
                            }
                            val sortedVectors
                            = similaritiesFilteredOutNaNs.sortedWith(vectorComparator).reversed()
                            val vecsToReturn
                            = if (sortedVectors.size < numberOfUsersRequested) sortedVectors
                                else sortedVectors.slice(IntRange(0, numberOfUsersRequested - 1))
                            callback.success(vecsToReturn, Util.dummyResponse)
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
    }

    private fun asyncGetInterestVectorOfUserList(parseUsers: List<ParseUser>,
                                                 index: Int, interestVectorsAccumulator:
                                                 ArrayList<Pair<Contact, Map<String, Double>>>,
                                                 callback: Callback<List<Pair<Contact, Map<String, Double>>>>) {
        if (index >= parseUsers.size) {
            callback.success(interestVectorsAccumulator, Util.dummyResponse)
        }
        else if (index < 0) {
            callback.failure(Util.INVALID_INDEX_ERROR)
        } else {
            val user = parseUsers.get(index)
            SongFeatures.asyncGetUserPlaylistFeatureMap(user, object: Callback<Map<String,Double>> {
                override fun success(playlistInterestVector: Map<String, Double>?,
                                     response: Response?) {
                    if (playlistInterestVector != null) {
                        interestVectorsAccumulator.add(Pair(Contact.fromParseUser(user),
                            playlistInterestVector))
                        asyncGetInterestVectorOfUserList(parseUsers,
                            index+1, interestVectorsAccumulator, callback)
                    }
                }
                override fun failure(error: RetrofitError?) {
                    callback.failure(error)
                }
            })
        }
    }

    private fun asyncFilterFriendsFromContacts(contactList: List<Contact>,
                                               callback: Callback<Pair<List<Contact>, List<Contact>>>) {
        val usersFollowedRelation = ParseUser.getCurrentUser().getRelation<ParseUser>(
            Util.PARSEUSER_KEY_USERS_FOLLOWED
        )
        asyncGetNonFriendParseUsers(contactList, 0, ArrayList<Contact>(),
            usersFollowedRelation, object: Callback<List<Contact>> {
            override fun success(nonFriendParseUserList: List<Contact>?, response: Response?) {
                if (nonFriendParseUserList != null) {
                    val nonFriendParseUsers = ArrayList<Contact>()
                    nonFriendParseUsers.addAll(nonFriendParseUserList)
                    val friendParseUserQuery = usersFollowedRelation.query
                    friendParseUserQuery.findInBackground { friendParseUsers, e ->
                        if (e != null) {
                            val throwable = Throwable(e.message.toString())
                            val error = retrofit.RetrofitError.unexpectedError(
                                Util.DUMMY_URL, throwable
                            )
                            callback.failure(error)
                        }
                        else if (friendParseUsers == null) {
                            callback.failure(Util.NULL_SUCCESS_ERROR)
                        }

                        followedFriends.addAll(friendParseUsers)
                        filledFollowedFriends = true

                        if (filledRecommendedSongs && displayingHomeFragment) {
                            goToHomeFragment()
                        }

                        val friendParseUserContacts = friendParseUsers.map{parseUser
                            -> Contact.fromParseUser(parseUser)}
                        callback.success(Pair(nonFriendParseUsers,
                            friendParseUserContacts), Util.dummyResponse)
                    }
                }
            }
            override fun failure(error: RetrofitError?) {
                Log.e(TAG, error?.message.toString())
                Log.e(TAG, error?.url.toString())
                error?.stackTraceToString()?.let { Log.e(TAG, it) }
            }
        })
    }

    private fun asyncExtractFriendPlaylists(friendParseUsers: List<ParseUser>,
                                            index: Int,
                                            accumulator: ArrayList<Pair<Contact, ArrayList<Song>>>,
                                            callback: Callback<ArrayList<Pair<Contact, ArrayList<Song>>>>) {
        if (index >= friendParseUsers.size) {
            callback.success(accumulator, Util.dummyResponse)
        }
        else if (index < 0) {
            callback.failure(Util.INVALID_INDEX_ERROR)
        } else {
            val parseUser = friendParseUsers.get(index)
            val playlistObj = parseUser.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
            val playlistSongsRelation = playlistObj?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
            val query = playlistSongsRelation?.getQuery()
            query?.addDescendingOrder(Util.PARSE_KEY_CREATED_AT)
            query?.findInBackground { objects, e ->
                if (e != null) callback.failure(
                    retrofit.RetrofitError.unexpectedError(Util.DUMMY_URL, Throwable(e.message)))
                else if (objects == null) callback.failure(Util.NULL_SUCCESS_ERROR)
                else {
                    val songs = ArrayList<Song>()
                    songs.addAll(objects)
                    if (songs.size > 0) {
                        accumulator.add(Pair(Contact.fromParseUser(parseUser), songs))
                    }
                    asyncExtractFriendPlaylists(friendParseUsers,
                        index+1, accumulator, callback)
                }
            }
        }
    }

    private fun asyncGetNonFriendParseUsers(contactList: List<Contact>,
                                            index: Int,
                                            accumulator: ArrayList<Contact>,
                                            usersFollowedRelation: ParseRelation<ParseUser>,
                                            callback: Callback<List<Contact>>) {
        if (index >= contactList.size) {
            callback.success(accumulator, Util.dummyResponse)
        }
        else if (index < 0) {
            callback.failure(Util.INVALID_INDEX_ERROR)
        } else {
            val contact = contactList.get(index)
            val usersFollowedQuery = usersFollowedRelation.query
            usersFollowedQuery.whereEqualTo(Util.PARSEUSER_KEY_PHONE_NUMBER, contact.phoneNumber)
            usersFollowedQuery.findInBackground { usersMatched, err ->
                if (err != null) {
                    val throwable = Throwable(err.message.toString())
                    val error = retrofit.RetrofitError.unexpectedError(
                        Util.DUMMY_URL, throwable
                    )
                    callback.failure(error)
                }
                else if (usersMatched == null) {
                    callback.failure(Util.NULL_SUCCESS_ERROR)
                }
                else if (usersMatched.size > 0) {
                    asyncGetNonFriendParseUsers(contactList,
                    index+1,
                    accumulator,
                    usersFollowedRelation,
                    callback)
                } else {
                    val query = ParseQuery.getQuery(ParseUser::class.java)
                    query.whereEqualTo(Util.PARSEUSER_KEY_PHONE_NUMBER, contact.phoneNumber)
                    query.findInBackground { objects, e ->
                        if (e != null) {
                            val throwable = Throwable(e.message.toString())
                            val error = retrofit.RetrofitError.unexpectedError(
                                Util.DUMMY_URL, throwable
                            )
                            callback.failure(error)
                        }
                        else if (objects == null) {
                            callback.failure(Util.NULL_SUCCESS_ERROR)
                        }
                        else if (objects.size > 0){
                            contact.parseUsername = objects.get(0).username
                            contact.parseUserId = objects.get(0).objectId
                            accumulator.add(contact)
                            asyncGetNonFriendParseUsers(contactList,
                                index+1,
                                accumulator,
                                usersFollowedRelation,
                                callback)
                        }
                        else {
                            asyncGetNonFriendParseUsers(contactList,
                                index+1,
                                accumulator,
                                usersFollowedRelation,
                                callback)
                        }
                    }
                }
            }
        }
    }

    inner class MainActivitySongController() : OldSongController {

        override fun logTrackInModel(trackId: String, weight: Int) {
            spotifyApi.service.getTrackAudioFeatures(trackId, object: Callback<AudioFeaturesTrack> {
                override fun success(t: AudioFeaturesTrack?, response: Response?) {
                    if (t != null) {
                        val featuresEntry = SongFeatures.fromAudioFeaturesTrack(t, weight)
                        featuresEntry.saveInBackground()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, it) }
                }
            })
        }

        fun addToParsePlaylist(track: Track, callback: Callback<Unit>){
            logTrackInModel(track.id, WEIGHT_ADDED_SONG_TO_PLAYLIST)
            isInPlaylist(track, object: Callback<Boolean>  {
                override fun success(trackInPlaylist: Boolean?, response: Response?) {
                    if (trackInPlaylist == null) {
                        Toast.makeText(this@MainActivity,
                            Util.THROWABLE_NULL_SUCCESS_MESSAGE, Toast.LENGTH_LONG
                        ).show()
                        callback.failure(Util.NULL_SUCCESS_ERROR)
                    }
                    else if (!trackInPlaylist) {
                        val newSong = Song.fromTrack(track)
                        newSong.saveInBackground {
                            if (it != null) {
                                Log.e(TAG, "error saving " + track.name + " to Parse")
                                callback.failure(retrofit.RetrofitError.unexpectedError(
                                    Util.DUMMY_URL, Throwable(it.message)
                                ))
                                return@saveInBackground
                            }
                            val user = ParseUser.getCurrentUser()
                            val playlist = user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
                            val playlistSongsRelation
                                    = playlist?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
                            playlistSongsRelation?.add(newSong)
                            parsePlaylistSongs.add(newSong)
                            playlist?.saveInBackground { e ->
                                if (e != null) {
                                    Log.e(TAG, "error adding " + track.name +
                                            " to parse playlist: " + e.message)
                                    callback.failure(retrofit.RetrofitError.unexpectedError(
                                        Util.DUMMY_URL, Throwable(e.message)
                                    ))
                                }
                                else {
                                    Log.d(TAG, "saving " + track.name + " to parse" +
                                            " playlist...")
                                    Toast.makeText(this@MainActivity,
                                        "Added " + track.name + " to playlist",
                                        Toast.LENGTH_SHORT).show()
                                    callback.success(Unit, Util.dummyResponse)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@MainActivity, TOAST_ALREADY_ADDED_SONG_TO_PLAYLIST,
                                Toast.LENGTH_LONG).show()
                        callback.success(Unit, Util.dummyResponse)
                    }
                }
                override fun failure(error: RetrofitError?) {
                    Toast.makeText(this@MainActivity,
                        "Error checking playlist: ${error?.message}", Toast.LENGTH_LONG).show()
                    callback.failure(error)
                }
            })
        }

        override fun addToPlaylist(track: Track, callback: Callback<Unit>) {
            addToParsePlaylist(track, callback)
        }

        override fun removeFromPlaylist(track: Track, callback: Callback<Unit>) {
            val position = findTrackPositionInParsePlaylist(track)
            if (position != -1) {
                removeFromParsePlaylist(track, position, callback)
            }
            else callback.failure(Util.INVALID_INDEX_ERROR)
        }

        private fun findTrackPositionInParsePlaylist(track: Track): Int {
            for (i in parsePlaylistSongs.indices) {
                val song = parsePlaylistSongs.get(i)
                if (song.getSpotifyId() == track.id) {
                    return i
                }
            }
            return -1
        }

        private fun removeFromParsePlaylist(track: Track, position: Int, callback: Callback<Unit>){
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
            val playlistSongsRelation = playlist?.getRelation<Song>(Util.PARSEUSER_KEY_FAVORITE_GENRES)
            if (parsePlaylistSongs.size <= position) {
                Log.d(TAG, "position " + position + " out of bounds?")
                Log.d(TAG, "parse playlist song size: " + parsePlaylistSongs.size)
                callback.failure(Util.INVALID_INDEX_ERROR)
                return
            }
            val songToDelete = parsePlaylistSongs.get(position)
            parsePlaylistSongs.removeAt(position)
            playlistSongsRelation?.remove(songToDelete)
            playlist?.saveInBackground {
                if (it != null) {
                    Log.e(TAG, "error removing " + track.name + " from playlist")
                    callback.failure(retrofit.RetrofitError.unexpectedError(
                        Util.DUMMY_URL, Throwable(it.message)
                    ))
                    return@saveInBackground
                }
                Toast.makeText(this@MainActivity,
                    "Removed ${track.name} from playlist",
                    Toast.LENGTH_LONG).show()
                callback.success(Unit, Util.dummyResponse)
                songToDelete.deleteInBackground()
            }
        }

        override fun removeFromPlaylistAtIndex(track: Track, position : Int) {
            removeFromParsePlaylist(track, position, object: Callback<Unit> {
                override fun success(t: Unit?, response: Response?) { }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, it) }
                }
            })
        }

        override fun isInPlaylist(track: Track, callback: Callback<Boolean>) {
            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
            val playlistSongsRelation
                    = playlist?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
            val playlistSongsQuery = playlistSongsRelation?.query
            playlistSongsQuery?.whereEqualTo(Util.PARSESONG_KEY_SPOTIFY_ID, track.id)
            playlistSongsQuery?.findInBackground { matchedSongs, relationQueryError ->
                if (relationQueryError != null) {
                    Log.e(TAG, "failed to query playlist songs: " + relationQueryError.message)
                    callback.failure(retrofit.RetrofitError.unexpectedError(Util.DUMMY_URL,
                        Throwable(relationQueryError.message)
                    ))
                }
                else if (matchedSongs == null) {
                    callback.failure(Util.NULL_SUCCESS_ERROR)
                }
                callback.success(matchedSongs.size > 0, Util.dummyResponse)
            }
        }

        fun addToSavedTracks(trackId: String) {
            spotifyApi.service.addToMySavedTracks(trackId, object: Callback<Any> {
                override fun success(t: Any?, response: Response?) {
                    Log.d(TAG, "added track " + trackId + " to saved tracks")
                }
                override fun failure(error: RetrofitError?) {
                    Log.e(TAG, "failed to add track " + trackId + " to saved songs: " +
                            error?.message)
                }
            })
        }

        override fun loadMoreSearchTracks(query: String, offset: Int, numberItemsToLoad: Int,
                                          adapter: TrackAdapter) {
            loadMoreQueryTracks(query, offset, numberItemsToLoad, false, adapter)
        }

        override fun loadMoreMixedHomeFeedItems(
            topTrackOffset: Int,
            numberItemsToLoad: Int,
            adapter: HomeFeedItemAdapter,
            swipeContainer: SwipeRefreshLayout?
        ) {
            val queryMap = mapOf(Util.SPOTIFY_QUERY_PARAM_LIMIT to numberItemsToLoad,
                Util.SPOTIFY_QUERY_PARAM_OFFSET to topTrackOffset)
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
                        Log.e(TAG, error?.message.toString())
                        swipeContainer?.isRefreshing = false
                    }
                })
        }

        override fun playSongOnSpotify(uri: String, spotifyId: String) {
            spotifyAppRemote?.playerApi?.play(uri);
            logTrackInModel(spotifyId, WEIGHT_PLAYED_SONG)
            spotifyApi.service.getTrack(spotifyId, object: Callback<Track> {
                override fun success(track: Track?, response: Response?) {
                    if (track != null) {
                        currentTrack = track
                        Log.d(TAG, "playing " + track.name)
                        miniPlayerFragment = MiniPlayerFragment.newInstance(track,
                            this@MainActivitySongController, false)
                        fragmentManager.beginTransaction().replace(R.id.miniPlayerFlContainer,
                            miniPlayerFragment!!).commit()
                        showMiniPlayerPreview()
                    }
                }
                override fun failure(error: RetrofitError?) {
                    error?.message?.let { Log.e(TAG, it) }
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
                    currentTrackIsPaused!!)
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

        fun showMiniPlayerPreview(){
            if (! (currentContact != null && displayingFriendsFragment)){
                val fragmentToShow = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
                if (fragmentToShow != null) {
                    fragmentManager.beginTransaction().show(fragmentToShow).commit()
                    miniPlayerFragmentContainer?.visibility = View.VISIBLE
                    showMiniPlayerFragment = true
                }
            }
        }

        fun hideMiniPlayerPreview(pauseSong: Boolean = true){
            val fragmentToHide = fragmentManager.findFragmentById(R.id.miniPlayerFlContainer)
            if (fragmentToHide != null) {
                fragmentManager.beginTransaction().hide(fragmentToHide).commit()
                if (pauseSong) { pauseSongOnSpotify() }
                showMiniPlayerFragment = false
            }
            miniPlayerFragmentContainer?.visibility = View.GONE
        }

        override fun logOutFromParse(){
            ParseUser.logOut()
            finish()
        }

        override fun exitSettingsTab() {
            val fragment = fragmentManager.findFragmentById(R.id.flContainer)
            if (fragment != null) {
                fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }

    }

    inner class MainActivityFriendsController() : FriendsController {
        val currentUser = ParseUser.getCurrentUser()
        val currentUserFollowedRelation
        = currentUser.getRelation<ParseUser>(Util.PARSEUSER_KEY_USERS_FOLLOWED)

        override fun followContact(contact: Contact, position: Int, adapter: TaggedContactAdapter) {
            val contactQuery = ParseQuery.getQuery(ParseUser::class.java)
            contactQuery.whereEqualTo(Contact.KEY_PHONE_NUMBER, contact.phoneNumber)
            contactQuery.findInBackground { objects, e ->
                if (e != null) {
                    e.message?.let { Log.e(TAG, it) }
                    return@findInBackground
                }
                if (objects != null) {
                    if (objects.size > 0) {
                        val userToFollow = objects.get(0)
                        currentUserFollowedRelation.add(userToFollow)
                        currentUser.saveInBackground{
                            if (it != null) {
                                it.message?.let { it1 -> Log.e(TAG, it1) }
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
                    e.message?.let { Log.e(TAG, it) }
                    return@findInBackground
                }
                if (objects != null) {
                    if (objects.size > 0) {
                        val userToFollow = objects.get(0)
                        currentUserFollowedRelation.remove(userToFollow)
                        currentUser.saveInBackground{
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
            showProgressBar()
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
                        hideProgressBar()
                    }
                }
            } else {
                goToFriendsPlaylistFragment(contact, animate)
                hideProgressBar()
            }

            bottomNavigationView.visibility = View.GONE
            mainActivitySongController.hideMiniPlayerPreview(false)
        }

        private fun goToFriendsPlaylistFragment(contact: Contact, animate: Boolean) {
            val newFragment = FriendPlaylistFragment.newInstance(currentContactPlaylist, mainActivitySongController)
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

    override fun onBackPressed() {
        if (currentContact != null && displayingFriendsFragment) {
            mainActivityFriendsController.exitDetailView()
        }
        else { super.onBackPressed() }
    }

    inner class UserRepository(): UserRepositoryInterface {
        private var userPlaylistSongs : ArrayList<UserRepositorySong> = ArrayList()
        private var currentSong: UserRepositorySong? = null
        private var currentSongIsPlaying: Boolean? = null

        override fun getUserPlaylistSongs(): ArrayList<UserRepositorySong> {
            return userPlaylistSongs
        }
        override fun addSongToUserPlaylist(song: UserRepositorySong) {
            val gson = Gson()
            val track = gson.fromJson(song.trackJsonData, Track::class.java)
            userPlaylistSongs.add(song)
            val newSong = Song.fromTrack(track)
            newSong.saveInBackground{ songSavingException ->
                if (songSavingException != null) {
                    userPlaylistSongs.remove(song)
                    return@saveInBackground
                }
                val user = ParseUser.getCurrentUser()
                val playlist = user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
                val playlistSongsRelation
                        = playlist?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
                playlistSongsRelation?.add(newSong)
                playlist?.saveInBackground { userPlaylistSavingException ->
                    if (userPlaylistSavingException != null) {
                        userPlaylistSongs.remove(song)
                    }
                    else {
                        parsePlaylistSongs.add(newSong)
                        Log.d(TAG, "added song succesffullyyyy: " + newSong.getName())
                    }
                }
            }
        }
        override fun removeSongFromUserPlaylist(songId: String) {
            val songToRemove = getSongWithId(songId)
            if (songToRemove != null) {
                userPlaylistSongs.remove(songToRemove)
            } else {
                // log error
            }

            val user = ParseUser.getCurrentUser()
            val playlist = user.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
            val playlistSongsRelation = playlist?.getRelation<Song>(Util.PARSEUSER_KEY_FAVORITE_GENRES)

            playlistSongsRelation?.query?.whereEqualTo(Util.PARSESONG_KEY_SPOTIFY_ID, songId)
                ?.findInBackground { matchedSongs, error ->
                    if (error != null || matchedSongs == null) {
                        // log error
                        return@findInBackground
                    }
                    if (matchedSongs.size == 0) {
                        // log error
                        return@findInBackground
                    }
                    else {
                        val parseSongToRemove = matchedSongs.get(0)
                        playlistSongsRelation.remove(parseSongToRemove)
                        playlist.saveInBackground {
                            parseException -> if (parseException != null) {
                                // log error
                            }
                            parseSongToRemove.deleteInBackground()
                        }
                    }
                }
        }
        override fun logSongInModel(song: UserRepositorySong, weight: Int) {
            val gson = Gson()
            val track = gson.fromJson(song.trackJsonData, Track::class.java)
            spotifyApi.service.getTrackAudioFeatures(track.id, object: Callback<AudioFeaturesTrack> {
                override fun success(t: AudioFeaturesTrack?, response: Response?) {
                    if (t != null) {
                        val featuresEntry = SongFeatures.fromAudioFeaturesTrack(t, weight)
                        featuresEntry.saveInBackground {
                            parseException -> if (parseException != null) {
                                Toast.makeText(this@MainActivity,
                                "Failed to log song: " + parseException.message,
                                Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun failure(error: RetrofitError?) {
                    Toast.makeText(this@MainActivity,
                        "Failed to log song: " + error?.message,
                        Toast.LENGTH_SHORT).show()
                }
            })
        }
        override fun isInUserPlaylist(songId: String): Boolean {
            val ids = userPlaylistSongs.map{song -> song.id}
            return songId in ids
        }

        override fun setCurrentSong(song: UserRepositorySong) {
            currentSong = song
        }

        override fun getCurrentSong(): UserRepositorySong? {
            return currentSong
        }

        override fun getCurrentSongIsPlaying(): Boolean? {
            return currentSongIsPlaying
        }

        override fun playSong(songId: String) {
            spotifyAppRemote?.playerApi?.play(Util.getSpotifyUriFromSpotifyId(songId))
        }
        override fun pauseSong() {
            spotifyAppRemote?.playerApi?.pause()
        }
        override fun resumeSong() {
            spotifyAppRemote?.playerApi?.resume()
        }

        override fun connectToPlayerState() {
//            this@MainActivity.connectToPlayerState()
        }

        private fun getSongWithId(id: String): UserRepositorySong? {
            for (song in userPlaylistSongs) {
                if (song.id == id){
                    return song
                }
            }
            return null
        }
    }
}

