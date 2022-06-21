package com.dev.moosic

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.Track.Factory.fromListItem
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.UserApi
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.ListItem
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import com.dev.moosic.Track

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    val CHILDREN_LIMIT = 1

    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"
    var mSpotifyAppRemote : SpotifyAppRemote? = null

    var reccomendedTracks : ArrayList<Track> =ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
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
                        addToPlaylist(mix1)
                        break
                    }
                }

//                Log.d(TAG, it.items.get(2).title + it.items.get(2).hasChildren)
//                it.items.map{ addToReccomendedSongs(it) }
            }
        }
    }

    private fun addToPlaylist(mix1: ListItem) {
        val children = mSpotifyAppRemote?.contentApi?.getChildrenOfItem(mix1, 1, 0)
        if (children != null){
            children.setResultCallback {
                for (item in it.items){
                    Log.d(TAG, item.hasChildren.toString())
                    val track = Track.fromListItem(item)
                    if (track != null){
                        reccomendedTracks.add(track)
                    }
                }
                // now send...
                for (item in reccomendedTracks){
                    item.mTitle?.let { it1 -> Log.d(TAG, it1 + " " + item.mImgUri) }
                }
            }
        }
    }

    private fun addToReccomendedSongs(item: ListItem) {
        if (reccomendedTracks.size >= 20){
            // send to fragment
            for (track in reccomendedTracks){
                Log.d(TAG, track.mTitle + " by " + track.mImgUri)
            }
            return
        }
        if (!item.hasChildren){
            val track = Track.fromListItem(item)
            if (track != null) reccomendedTracks.add(track)
        }
        val children = mSpotifyAppRemote?.contentApi?.getChildrenOfItem(item, CHILDREN_LIMIT, 0)
        if (children != null) {
            children.setResultCallback {
                it.items.map{
                    addToReccomendedSongs(it)
                }
            }
        }
        Log.d(TAG, reccomendedTracks.size.toString()) // recursively?
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        mSpotifyAppRemote = null
    }
}