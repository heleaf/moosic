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


private const val ARG_PARAM1 = "playlistSongs"
private const val ARG_PARAM2 = "buttonsToShow"

/**
 * A simple [Fragment] subclass.
 * Use the [ParsePlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class ParsePlaylistFragment(controller : MainActivity.MainActivitySongController) : Fragment() {
    private var songs: ArrayList<Song> = ArrayList()
    private var mainActivityController = controller
    private var buttonsToShow: List<String> = ArrayList()

    var rvPlaylistTracks : RecyclerView? = null
    var adapter : SongAdapter? = null
    var scrollListener : EndlessRecyclerViewScrollListener? = null
    var swipeContainer: SwipeRefreshLayout? = null

    var playlistTitle: TextView? = null
    var playlistDescription: TextView? = null
    var playlistAuthor: TextView? = null

    var emptyPlaylistText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songs = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            buttonsToShow = it.getStringArrayList(ARG_PARAM2)!!
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
        fun newInstance(playlistSongs: ArrayList<Song>, controller: MainActivity.MainActivitySongController,
                        buttonsToShow: ArrayList<String>) =
            ParsePlaylistFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(playlistSongs))
                    putStringArrayList(ARG_PARAM2, buttonsToShow)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Hide playlist title elements for now
        playlistTitle = view.findViewById(R.id.playlistTitle)
        playlistAuthor = view.findViewById(R.id.playlistAuthor)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        listOf(playlistTitle, playlistAuthor, playlistDescription).map{ tv -> tv?.visibility = View.GONE }

        emptyPlaylistText = view.findViewById(R.id.emptyPlaylistText)

        rvPlaylistTracks = view.findViewById(R.id.rvPlaylistTracks)

        adapter = SongAdapter(view.context,
            songs, mainActivityController, buttonsToShow, emptyPlaylistText)

        rvPlaylistTracks?.adapter = adapter

        // TODO sticky headers
//        val recyclerItemDecoration : RecyclerItemDecoration
//        = RecyclerItemDecoration(requireContext(), resources.getDimensionPixelSize(R.dimen.header_height),
//        true, getSectionCallback(songs))
//        rvPlaylistTracks?.addItemDecoration(recyclerItemDecoration)

        val linearLayoutManager = LinearLayoutManager(context)
        rvPlaylistTracks?.setLayoutManager(linearLayoutManager)

        swipeContainer = view.findViewById(R.id.profileFeedSwipeContainer)
        swipeContainer?.setOnRefreshListener {
            // TODO: refresh the playlist
            swipeContainer?.isRefreshing = false
            scrollListener?.resetState()
        }

        swipeContainer?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )

        // TODO: add infinite pagination
//        scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
//            override fun onLoadMore(offset: Int, totalItemsCount: Int, view: RecyclerView?) {
//                mainActivityController.loadMorePlaylistSongs(tracks.size, totalItemsCount, adapter!!)
//            }
//        })
//        rvPlaylistTracks?.addOnScrollListener(scrollListener!!);

    }

    inner class DecorationSectionCallback(songs: ArrayList<Song>) : RecyclerItemDecoration.SectionCallback {
        override fun isHeader(position: Int): Boolean {
            // TODO: return pos == 0 || true if the category of current ! = category of previous
            // list.get(pos).get("Title")!=list.get(pos-1).get("Title");
            return position == 0 || position == 4
        }

        override fun getSectionHeaderName(position: Int): String {
            // TODO: index in at the position and grab the name
            return if (position < 4) " pain " else " suffering "
//            return songs.get(position).getName()!!
            // "testing" // if ( position < 4 ) songs.get(position).getName()!! else "hello"
        }
    }

    fun getSectionCallback(songs : ArrayList<Song>) : RecyclerItemDecoration.SectionCallback {
        return DecorationSectionCallback(songs)
    }
}