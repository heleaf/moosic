package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.R
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.adapters.ViewOnlyTrackAdapter
import com.dev.moosic.models.UserRepositorySong
import org.parceler.Parcels

private const val ARG_SONGS = "songs"

class OfflineHomeFeedFragment : Fragment() {
    lateinit var songs: ArrayList<UserRepositorySong>
    lateinit var songsrv: RecyclerView
    lateinit var adapter: ViewOnlyTrackAdapter
    lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songs = Parcels.unwrap(it.getParcelable(ARG_SONGS))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_feed, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(songs: ArrayList<UserRepositorySong>) =
            OfflineHomeFeedFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SONGS, Parcels.wrap(songs))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songsrv = view.findViewById(R.id.rvTopTracks)
        adapter = ViewOnlyTrackAdapter(view.context, songs)
        songsrv.adapter = adapter

        val linearLayoutManager = LinearLayoutManager(context)
        songsrv.layoutManager = linearLayoutManager

        swipeContainer = view.findViewById(R.id.homeFeedSwipeContainer)
        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = false
        }

    }
}