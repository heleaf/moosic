package com.dev.moosic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.*
import com.dev.moosic.MainActivity
import com.dev.moosic.adapters.SongAdapter
import com.dev.moosic.models.Song
import org.parceler.Parcels


private const val ARG_PLAYLIST_SONGS = "playlistSongs"
private const val ARG_BUTTONS_TO_SHOW = "buttonsToShow"

open class ParsePlaylistFragment(controller : MainActivity.MainActivitySongController) : Fragment() {
    private var songs: ArrayList<Song> = ArrayList()
    private var mainActivityController = controller
    private var buttonsToShow: List<String> = ArrayList()

    private lateinit var rvPlaylistTracks : RecyclerView
    lateinit var adapter : SongAdapter
    private lateinit var swipeContainer: SwipeRefreshLayout

    private lateinit var playlistTitle: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var playlistAuthor: TextView

    private lateinit var emptyPlaylistText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songs = Parcels.unwrap(it.getParcelable(ARG_PLAYLIST_SONGS))
            buttonsToShow = it.getStringArrayList(ARG_BUTTONS_TO_SHOW)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_library, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(playlistSongs: ArrayList<Song>,
                        controller: MainActivity.MainActivitySongController,
                        buttonsToShow: ArrayList<String>) =
            ParsePlaylistFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PLAYLIST_SONGS, Parcels.wrap(playlistSongs))
                    putStringArrayList(ARG_BUTTONS_TO_SHOW, buttonsToShow)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistTitle = view.findViewById(R.id.playlistTitle)
        playlistAuthor = view.findViewById(R.id.playlistAuthor)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        listOf(playlistTitle, playlistAuthor, playlistDescription).map{
                tv -> tv.visibility = View.GONE }

        emptyPlaylistText = view.findViewById(R.id.emptyPlaylistText)

        rvPlaylistTracks = view.findViewById(R.id.rvPlaylistTracks)

        adapter = SongAdapter(view.context,
            songs, mainActivityController, buttonsToShow, emptyPlaylistText)

        rvPlaylistTracks.adapter = adapter

        val linearLayoutManager = LinearLayoutManager(context)
        rvPlaylistTracks.setLayoutManager(linearLayoutManager)

        swipeContainer = view.findViewById(R.id.profileFeedSwipeContainer)
        swipeContainer.setOnRefreshListener {
            swipeContainer.isRefreshing = false
        }

        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

    }
}