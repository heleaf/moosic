package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dev.moosic.R
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "currentSong"

/**
 * A simple [Fragment] subclass.
 * Use the [MiniPlayerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MiniPlayerFragment : Fragment() {
    val TAG = "MiniPlayerFragment"
    private var currentTrack: Track? = null

    var trackTitle: TextView? = null
    var trackArtist: TextView? = null
    var trackAlbumCover: SimpleDraweeView? = null
    var playPauseButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTrack = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            Log.d(TAG, "got current track: " + currentTrack?.name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mini_player, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MiniPlayerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(currentTrack: Track) =
            MiniPlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(currentTrack))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        trackTitle = view.findViewById(R.id.miniPlayerSongTitle)
        trackArtist = view.findViewById(R.id.miniPlayerSongArtist)
        trackAlbumCover = view.findViewById(R.id.miniPlayerAlbumCover)
        playPauseButton = view.findViewById(R.id.miniPlayerPlayPauseButton)

        trackTitle?.setText(currentTrack?.name)
        val artistNameText = currentTrack?.artists?.fold(
            ""
        ) { accumulator, artist ->
            if (artist.name == currentTrack?.artists?.get(0)?.name) artist.name else
                accumulator + ", " + artist.name
        }
        trackArtist?.setText(artistNameText)

        try {
            val albumCoverImgUri = currentTrack?.album?.images?.get(0)?.url
            trackAlbumCover?.setImageURI(albumCoverImgUri);
        } catch (e : Exception) {
            Log.e(TAG, "error: " + e.message)
        }
    }
}