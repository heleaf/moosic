package com.dev.moosic

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.dev.moosic.fragments.MiniPlayerFragment
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

class MiniPlayerDetailActivity : AppCompatActivity() {
    val TAG = "MiniPlayerDetailActivity"

    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI = "http://localhost:8080"

    var mSpotifyAppRemote : SpotifyAppRemote? = null
    var playerStateSubscription: Subscription<PlayerState>? = null

    var currentTrack: com.spotify.protocol.types.Track? = null
    var currentTrackIsPaused: Boolean? = null

    var albumCover: ImageView? = null
    var songTitle: TextView? = null
    var songArtist: TextView? = null
    var seekBar: SeekBar? = null
    var currentTime: TextView? = null
    var totalTime: TextView? = null
    var playPauseButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mini_player_detail)

        albumCover = findViewById(R.id.miniPlayerDetailImg)
        songTitle = findViewById(R.id.miniPlayerDetailTitle)
        songArtist = findViewById(R.id.miniPlayerDetailArtist)
        seekBar = findViewById(R.id.miniPlayerSeekBar)
        currentTime = findViewById(R.id.miniPlayerDetailCurrentTime)
        totalTime = findViewById(R.id.miniPlayerTotalTime)
        playPauseButton = findViewById(R.id.miniPlayerDetailPlayPause)

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
                    Log.d("MiniPlayerDetailActivity", "Connected! Yay!")
                    // Now you can start interacting with App Remote
                    connected()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MiniPlayerDetailActivity", throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
    }

    fun connected() {
        playerStateSubscription = mSpotifyAppRemote!!.playerApi.subscribeToPlayerState()
        playerStateSubscription?.setEventCallback { playerState: PlayerState ->
            val track: com.spotify.protocol.types.Track? = playerState.track
            if (track != null) {
                if (track.name != currentTrack?.name) {
                    // update the view
                    currentTrack = track
                    currentTrackIsPaused = playerState.isPaused
                    updateView()

                }
                else if (playerState.isPaused != currentTrackIsPaused) {
                    // update the view
                    currentTrackIsPaused = playerState.isPaused
                }
            }
        }
    }

    private fun updateView() {
        if (currentTrack != null) {
            songTitle?.setText(currentTrack?.name)

            var artistString = ""
            for (artist in currentTrack!!.artists){
                artistString = artistString + ", " + artist.name
            }
            songArtist?.setText(artistString)
//            Log.d(TAG, "uri: " + currentTrack!!.imageUri.raw)
//            albumCover?.setImageURI(Uri.parse(currentTrack!!.imageUri.raw))
        }
    }
}