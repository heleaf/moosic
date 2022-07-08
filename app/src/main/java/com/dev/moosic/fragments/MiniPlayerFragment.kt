package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_PARAM1 = "currentSong"
private const val ARG_PARAM2 = "isPaused"
private const val ARG_PARAM3 = "controller"

/**
 * A simple [Fragment] subclass.
 * Use the [MiniPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MiniPlayerFragment(controller: MainActivity.MainActivitySongController) : Fragment() {
    val TAG = "MiniPlayerFragment"
    private var currentTrack: Track? = null

//    lateinit var mainActivityController : MainActivity.MainActivityController
////
//    constructor(controller: MainActivity.MainActivityController) : this() {
//        mainActivityController = controller
//    }

    val mainActivityController = controller
//    constructor() : this() {
//        mainActivityController = null
//    }

    var trackTitle: TextView? = null
    var trackArtist: TextView? = null
    var trackAlbumCover: SimpleDraweeView? = null
    var playPauseButton: ImageView? = null
    var layout: ConstraintLayout? = null

    var closeMiniPlayerButton: ImageButton? = null

    var isPaused: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTrack = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
//            Log.d(TAG, "got current track: " + currentTrack?.name)
            val id = currentTrack?.uri?.slice(IntRange(14, currentTrack!!.uri.length - 1))
//            Log.d(TAG, "uri: " + currentTrack?.uri)
//            Log.d(TAG, "id: " + id)
            isPaused = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance(currentTrack: Track, controller: MainActivity.MainActivitySongController,
                        isPaused: Boolean) =
            MiniPlayerFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(currentTrack))
                    putBoolean(ARG_PARAM2, isPaused)
//                    putParcelable(ARG_PARAM3, Parcels.wrap(controller))

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

        closeMiniPlayerButton?.setOnClickListener {
            mainActivityController.hideMiniPlayerPreview()
        }

        if (isPaused){
            playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
        } else playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)

        playPauseButton?.setOnClickListener {
            if (isPaused) {
                mainActivityController.resumeSongOnSpotify()
            } else {
                mainActivityController.pauseSongOnSpotify()
            }
            isPaused = !isPaused
        }

        layout?.setOnClickListener{
            mainActivityController.goToMiniPlayerDetailView()
        }

        trackTitle?.setText(currentTrack?.name)
        trackTitle?.isSelected = true
        val artistNameText = currentTrack?.artists?.fold(
            ""
        ) { accumulator, artist ->
            if (artist.name == currentTrack?.artists?.get(0)?.name) artist.name else
                accumulator + ", " + artist.name
        }
        trackArtist?.setText(artistNameText)
        trackArtist?.isSelected = true

        try {
            val albumCoverImgUri = currentTrack?.album?.images?.get(0)?.url
            trackAlbumCover?.setImageURI(albumCoverImgUri);
        } catch (e : Exception) {
            Log.e(TAG, "error: " + e.message)
        }
    }
}