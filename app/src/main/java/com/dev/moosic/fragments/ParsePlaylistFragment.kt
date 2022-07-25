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
import com.dev.moosic.adapters.SongAdapter
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.Song
import org.parceler.Parcels


private const val ARG_PLAYLIST_SONGS = "playlistSongs"

open class ParsePlaylistFragment(private val mainActivitySongController : MainActivityControllerInterface,
                                 private val playlistController: UserRepoPlaylistControllerInterface) : Fragment() {
    private var songs: ArrayList<Song> = ArrayList()

    private lateinit var rvPlaylistTracks : RecyclerView
    lateinit var adapter : SongAdapter
    private lateinit var swipeContainer: SwipeRefreshLayout

    private lateinit var emptyPlaylistText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songs = Parcels.unwrap(it.getParcelable(ARG_PLAYLIST_SONGS))
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
                        mainActivitySongController: MainActivityControllerInterface,
                        playlistController: UserRepoPlaylistControllerInterface) =
            ParsePlaylistFragment(mainActivitySongController, playlistController).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PLAYLIST_SONGS, Parcels.wrap(playlistSongs))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyPlaylistText = view.findViewById(R.id.emptyPlaylistText)
        if (songs.size == 0) emptyPlaylistText.visibility = View.GONE

        rvPlaylistTracks = view.findViewById(R.id.rvPlaylistTracks)

        adapter = SongAdapter(view.context,
            mainActivitySongController, emptyPlaylistText, playlistController)

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