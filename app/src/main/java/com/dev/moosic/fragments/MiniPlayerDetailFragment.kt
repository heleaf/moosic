package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "currentTrack"

/**
 * A simple [Fragment] subclass.
 * Use the [MiniPlayerDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MiniPlayerDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var currentTrack: Track? = null
    var trackTitle: TextView? = null
    var trackArtist: TextView? = null
    var trackAlbumCover: SimpleDraweeView? = null
    var playPauseButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentTrack = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mini_player_detail, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MiniPlayerDetailFragment.
         */
        @JvmStatic
        fun newInstance(track: Track) =
            MiniPlayerDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(track))
                }
            }
    }
}