package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.dev.moosic.R
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels
import java.lang.Exception

private const val ARG_CURRENT_TRACK = "currentTrack"
private const val ARG_IS_PAUSED = "isPaused"
private const val TAG = "MiniPlayerDetailFragment"

private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class MiniPlayerDetailFragment(private val mainActivitySongController: MainActivityControllerInterface,
                               private val playlistController: UserRepoPlaylistControllerInterface) : Fragment() {
    private lateinit var currentTrack: Track
    lateinit var trackTitle: TextView
    lateinit var trackArtist: TextView
    lateinit var trackAlbumCover: SimpleDraweeView
    lateinit var playPauseButton: ImageView
    lateinit var backToHome: ImageButton
    lateinit var addToPlaylistButton: ImageView

    lateinit var seekBar: SeekBar
    lateinit var currentTime: TextView
    lateinit var totalTime : TextView

    var isPaused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTrack = Parcels.unwrap(it.getParcelable(ARG_CURRENT_TRACK))
            isPaused = it.getBoolean(ARG_IS_PAUSED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mini_player_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.miniPlayerSeekBar)
        currentTime = view.findViewById(R.id.miniPlayerDetailCurrentTime)
        totalTime = view.findViewById(R.id.miniPlayerTotalTime)
        seekBar.visibility = View.GONE
        currentTime.visibility = View.GONE
        totalTime.visibility = View.GONE

        trackTitle = view.findViewById(R.id.miniPlayerDetailTitle)
        trackArtist = view.findViewById(R.id.miniPlayerDetailArtist)
        trackAlbumCover = view.findViewById(R.id.miniPlayerDetailImg)
        playPauseButton = view.findViewById(R.id.miniPlayerDetailPlayPause)
        backToHome = view.findViewById(R.id.miniPlayerDetailBackToHome)

        addToPlaylistButton = view.findViewById(R.id.miniPlayerDetailAddToPlaylistButton)

        val gson = Gson()
        addToPlaylistButton.setOnClickListener {
            currentTrack.let {
                playlistController.addToPlaylist(UserRepositorySong(currentTrack.id,
                gson.toJson(currentTrack).toString()), true)
            }
        }

        backToHome.setOnClickListener {
            mainActivitySongController.exitMiniPlayerDetailView()
        }

        if (isPaused){
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            trackAlbumCover.clearAnimation()
        } else {
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            trackAlbumCover.startAnimation(
                AnimationUtils.loadAnimation(activity, R.anim.rotate_indefinitely) );
        }

        playPauseButton.setOnClickListener {
            if (isPaused) {
                mainActivitySongController.resumeSongOnSpotify()
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                trackAlbumCover.startAnimation(
                    AnimationUtils.loadAnimation(activity, R.anim.rotate_indefinitely) )
            } else {
                mainActivitySongController.pauseSongOnSpotify()
                playPauseButton.setImageResource(android.R.drawable.ic_media_play)
                trackAlbumCover.clearAnimation()
            }
            isPaused = !isPaused
        }

        trackTitle.setText(currentTrack.name)
        val artistText = currentTrack.artists?.fold(
            EMPTY_STR
        ) { acc, artist ->
            if (acc == EMPTY_STR) artist.name else acc + ARTIST_STR_SEPARATOR + artist.name
        }

        trackArtist.setText(artistText)
        try {
            val albumCoverImgUri = currentTrack.album.images.get(0).url
            trackAlbumCover.setImageURI(albumCoverImgUri);
        } catch (e : Exception) {
            e.message?.let { Log.e(TAG, it) }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(track: Track, mainActivitySongController: MainActivityControllerInterface,
                        isPaused: Boolean, playlistController: UserRepoPlaylistControllerInterface) =
            MiniPlayerDetailFragment(mainActivitySongController, playlistController).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CURRENT_TRACK, Parcels.wrap(track))
                    putBoolean(ARG_IS_PAUSED, isPaused)
                }
            }
    }
}