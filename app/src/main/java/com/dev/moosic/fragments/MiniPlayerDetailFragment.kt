package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "currentTrack"
private const val ARG_PARAM2 = "isPaused"

/**
 * A simple [Fragment] subclass.
 * Use the [MiniPlayerDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MiniPlayerDetailFragment(controller: MainActivity.MainActivityController) : Fragment() {
    val TAG = "MiniPlayerDetailFragment"

    private var currentTrack: Track? = null
    var trackTitle: TextView? = null
    var trackArtist: TextView? = null
    var trackAlbumCover: SimpleDraweeView? = null
    var playPauseButton: ImageView? = null
    var backToHome: ImageButton? = null

    var seekBar: SeekBar? = null
    var currentTime: TextView? = null
    var totalTime : TextView? = null

    var isPaused: Boolean = false

    val mainActivityController = controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTrack = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            isPaused = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mini_player_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.miniPlayerSeekBar)
        currentTime = view.findViewById(R.id.miniPlayerDetailCurrentTime)
        totalTime = view.findViewById(R.id.miniPlayerTotalTime)
        seekBar?.visibility = View.GONE
        currentTime?.visibility = View.GONE
        totalTime?.visibility = View.GONE

        trackTitle = view.findViewById(R.id.miniPlayerDetailTitle)
        trackArtist = view.findViewById(R.id.miniPlayerDetailArtist)
        trackAlbumCover = view.findViewById(R.id.miniPlayerDetailImg)
        playPauseButton = view.findViewById(R.id.miniPlayerDetailPlayPause)
        backToHome = view.findViewById(R.id.miniPlayerDetailBackToHome)

        backToHome?.setOnClickListener {
            mainActivityController.exitMiniPlayerDetailView()
        }

        if (isPaused){
            playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
        } else playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)

        playPauseButton?.setOnClickListener {
            if (isPaused) {
                mainActivityController.resumeSongOnSpotify()
                playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                mainActivityController.pauseSongOnSpotify()
                playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
            }
            isPaused = !isPaused
        }

        trackTitle?.setText(currentTrack?.name)
        val artistText = currentTrack?.artists?.fold(
            ""
        ) { acc, artist ->
            if (acc == "") artist.name else acc + ", " + artist.name
        }
        trackArtist?.setText(artistText)

        try {
            val albumCoverImgUri = currentTrack?.album?.images?.get(0)?.url
            trackAlbumCover?.setImageURI(albumCoverImgUri);
        } catch (e : Exception) {
            Log.e(TAG, "error: " + e.message)
        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MiniPlayerDetailFragment.
         */
        @JvmStatic
        fun newInstance(track: Track, controller: MainActivity.MainActivityController,
            isPaused: Boolean) =
            MiniPlayerDetailFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(track))
                    putBoolean(ARG_PARAM2, isPaused)
                }
            }
    }
}