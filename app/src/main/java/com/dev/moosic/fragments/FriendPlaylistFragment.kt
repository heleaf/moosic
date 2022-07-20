package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.adapters.TrackAdapter
import com.dev.moosic.controllers.OldSongController
import com.dev.moosic.models.Contact
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_TRACKS = "tracks"

class FriendPlaylistFragment(controller: OldSongController) : Fragment() {
    private val songController: OldSongController = controller
    private var tracks = ArrayList<Track>()
    private lateinit var rvSongs: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var emptyPlaylistText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tracks = Parcels.unwrap(it.getParcelable(ARG_TRACKS))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_playlist, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(tracks : ArrayList<Track>, controller: OldSongController) =
            FriendPlaylistFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRACKS, Parcels.wrap(tracks))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvSongs = view.findViewById(R.id.userDetailSongsRv)
        adapter = TrackAdapter(view.context, tracks, songController)
        rvSongs.adapter = adapter

        val linearLayoutManager = LinearLayoutManager(context)
        rvSongs.layoutManager = linearLayoutManager

        emptyPlaylistText = view.findViewById(R.id.emptyUserPlaylistText)

        if (tracks.isEmpty()) {
            emptyPlaylistText.visibility = View.VISIBLE
        } else {
            emptyPlaylistText.visibility = View.GONE
        }
    }
}