package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.dev.moosic.R
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_CURRENT_TRACK = "currentTrack"
private const val ARG_IS_PAUSED = "isPaused"
private const val TAG = "MiniPlayerFragment"

class MiniPlayerFragment(private val mainActivitySongController: MainActivityControllerInterface,
                         private val playlistController: UserRepoPlaylistControllerInterface) : Fragment() {
    private lateinit var currentTrack: Track
    private lateinit var trackTitle: TextView
    lateinit var trackArtist: TextView
    lateinit var trackAlbumCover: SimpleDraweeView
    lateinit var playPauseButton: ImageButton
    lateinit var layout: ConstraintLayout
    lateinit var closeMiniPlayerButton: ImageButton
    lateinit var addToPlaylistButton: ImageButton
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
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(currentTrack: Track, mainActivitySongController: MainActivityControllerInterface,
                        isPaused: Boolean, playlistController: UserRepoPlaylistControllerInterface) =
            MiniPlayerFragment(mainActivitySongController, playlistController).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CURRENT_TRACK, Parcels.wrap(currentTrack))
                    putBoolean(ARG_IS_PAUSED, isPaused)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackTitle = view.findViewById(R.id.miniPlayerSongTitle)
        trackArtist = view.findViewById(R.id.miniPlayerSongArtist)
        trackAlbumCover = view.findViewById(R.id.miniPlayerAlbumCover)
        playPauseButton = view.findViewById(R.id.miniPlayerPlayPauseButton)
        layout = view.findViewById(R.id.miniPlayerConstraintLayout)
        closeMiniPlayerButton = view.findViewById(R.id.closeMiniPlayerButton)
        addToPlaylistButton = view.findViewById(R.id.miniPlayerPreviewAddToPlaylistButton)

        val gson = Gson()
        addToPlaylistButton.setOnClickListener {
            currentTrack.let {
                playlistController.addToPlaylist(UserRepositorySong(currentTrack.id,
                gson.toJson(currentTrack).toString()), true, true)
            }
        }

        closeMiniPlayerButton.setOnClickListener {
            mainActivitySongController.hideMiniPlayerPreview()
        }

        if (isPaused){
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        } else playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

        playPauseButton.setOnClickListener {
            if (isPaused) {
                mainActivitySongController.resumeSongOnSpotify()
            } else {
                mainActivitySongController.pauseSongOnSpotify()
            }
            isPaused = !isPaused
        }

        layout.setOnClickListener{
            mainActivitySongController.goToMiniPlayerDetailView()
        }

        trackTitle.setText(currentTrack.name)
        trackTitle.isSelected = true
        val artistNameText = currentTrack.artists?.fold(
            ""
        ) { accumulator, artist ->
            if (artist.name == currentTrack.artists?.get(0)?.name) artist.name else
                accumulator + ", " + artist.name
        }
        trackArtist.setText(artistNameText)
        trackArtist.isSelected = true

        try {
            val albumCoverImgUri = currentTrack.album.images.get(0).url
            trackAlbumCover.setImageURI(albumCoverImgUri);
        } catch (e : Exception) {
            e.message?.let { Log.e(TAG, it) }
        }
    }
}