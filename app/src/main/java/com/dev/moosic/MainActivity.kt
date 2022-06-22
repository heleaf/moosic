package com.dev.moosic

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.fragments.HomeFeedFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote

import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.Pager
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    // Spotify Android SDK
    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"

    var mSpotifyAppRemote : SpotifyAppRemote? = null
    var topTracks : ArrayList<kaaes.spotify.webapi.android.models.Track> = ArrayList()
    val spotifyApi = SpotifyApi()

    var bottomNavigationView : BottomNavigationView? = null
    val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val token = getIntent().getExtras()?.getString("accessToken")
        spotifyApi.setAccessToken(token)
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

        bottomNavigationView?.selectedItemId = R.id.actionHome
    }

    private fun setUpProfileFragment() {
        Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show()
    }

    private fun setUpSearchFragment() {
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show()
    }

    private fun setUpHomeFragment() {
        val spotifyApiService = spotifyApi.service
        spotifyApiService?.getTopTracks(object : Callback<Pager<kaaes.spotify.webapi.android.models.Track>> {
            override fun success(
                t: Pager<kaaes.spotify.webapi.android.models.Track>?,
                response: Response?
            ) {
                if (t != null){
                    Log.d(TAG, "success: " + t.toString() + " size: " + t.items.size)
                    topTracks.clear()
                    topTracks.addAll(t.items)
                    // make a transaction
                    val homeFragment = HomeFeedFragment.newInstance(topTracks)
                    fragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).commit()
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
//
//        spotifyApiService?.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", object : Callback< Album?> {
//            override fun success(album: Album?, response: Response?) {
//                if (album != null) {
//                    Log.d("Album success", album.name)
//                }
//            }
//            override fun failure(error: RetrofitError) {
//                Log.d("Album failure", error.toString())
//            }
//        })


        /*
        if (mSpotifyAppRemote != null) SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // Set the connection parameters
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d(TAG, "Connected! Yay!")

                    // Now you can start interacting with App Remote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, throwable.message, throwable)

                    // Something went wrong when attempting to connect! Handle errors here

                    // TODO: use kotlin
//                    if (error instanceof NotLoggedInException || error instanceof UserNotAuthorizedException) {
//                        // Show login button and trigger the login flow from auth library when clicked
//                    } else if (error instanceof CouldNotFindSpotifyApp) {
//                        // Show button to download Spotify
//                    }
                }
            })
         */
    }

    private fun connected() {
//        TODO("Not yet implemented")
        Log.d(TAG, "connected")
//        mSpotifyAppRemote?.getPlayerApi()?.toString()?.let { Log.d(TAG, it) }
//        mSpotifyAppRemote?.getPlayerApi()?.play("spotify:playlist:37i9dQZF1DX7K31D69s4M1");
//
//        mSpotifyAppRemote!!.playerApi
//            .subscribeToPlayerState()
//            .setEventCallback { playerState: PlayerState ->
//                val track: Track? = playerState.track
//                if (track != null) {
//                    Log.d("MainActivity", track.name.toString() + " by " + track.artist.name)
//                }
//            }
//
//        val userApi = mSpotifyAppRemote?.getUserApi()
//        if (userApi != null){
//            // test
//        }
        
        // this only contains playlists...
        val contentApi = mSpotifyAppRemote?.getContentApi()
        if (contentApi != null){
            var reccomended = contentApi.getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
            reccomended.setResultCallback {
                // access the data once it comes back-- it never makes it here though
                if (it != null){
                    Log.d(TAG, it.toString())
                }

                for (item in it.items){
                    if (item.title.contains("Made For")){
                        // pull its songs
                        val mix1 = item
//                        addToPlaylist(mix1)
                        break
                    }
                }

//                Log.d(TAG, it.items.get(2).title + it.items.get(2).hasChildren)
//                it.items.map{ addToReccomendedSongs(it) }
            }
        }
    }

//    private fun addToPlaylist(mix1: ListItem) {
//        val children = mSpotifyAppRemote?.contentApi?.getChildrenOfItem(mix1, 1, 0)
//        if (children != null){
//            children.setResultCallback {
//                for (item in it.items){
//                    Log.d(TAG, item.hasChildren.toString())
//                    val track = Track.fromListItem(item)
//                    if (track != null){
//                        reccomendedTracks.add(track)
//                    }
//                }
//                // now send...
//                for (item in reccomendedTracks){
//                    item.mTitle?.let { it1 -> Log.d(TAG, it1 + " " + item.mImgUri) }
//                }
//            }
//        }
//    }

//    private fun addToReccomendedSongs(item: ListItem) {
//        if (reccomendedTracks.size >= 20){
//            // send to fragment
//            for (track in reccomendedTracks){
//                Log.d(TAG, track.mTitle + " by " + track.mImgUri)
//            }
//            return
//        }
//        if (!item.hasChildren){
//            val track = Track.fromListItem(item)
//            if (track != null) reccomendedTracks.add(track)
//        }
//        val children = mSpotifyAppRemote?.contentApi?.getChildrenOfItem(item, CHILDREN_LIMIT, 0)
//        if (children != null) {
//            children.setResultCallback {
//                it.items.map{
//                    addToReccomendedSongs(it)
//                }
//            }
//        }
//        Log.d(TAG, reccomendedTracks.size.toString()) // recursively?
//    }

    override fun onStop() {
        super.onStop()
//        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
//        mSpotifyAppRemote = null
    }
}