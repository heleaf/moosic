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
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_CURRENT_TRACK = "currentTrack"
private const val ARG_IS_PAUSED = "isPaused"
private const val TAG = "MiniPlayerFragment"

class MiniPlayerFragment(controller: MainActivity.MainActivitySongController) : Fragment() {
    private lateinit var currentTrack: Track
    private val mainActivityController = controller
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
        fun newInstance(currentTrack: Track, controller: MainActivity.MainActivitySongController,
                        isPaused: Boolean) =
            MiniPlayerFragment(controller).apply {
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

        addToPlaylistButton.setOnClickListener {
            currentTrack.let { it1 -> mainActivityController.addToParsePlaylist(it1) }
        }

        closeMiniPlayerButton.setOnClickListener {
            mainActivityController.hideMiniPlayerPreview()
        }

        if (isPaused){
            playPauseButton.setImageResource(android.R.drawable.ic_media_play)
        } else playPauseButton.setImageResource(android.R.drawable.ic_media_pause)

        playPauseButton.setOnClickListener {
            if (isPaused) {
                mainActivityController.resumeSongOnSpotify()
            } else {
                mainActivityController.pauseSongOnSpotify()
            }
            isPaused = !isPaused
        }

        layout.setOnClickListener{
            mainActivityController.goToMiniPlayerDetailView()
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