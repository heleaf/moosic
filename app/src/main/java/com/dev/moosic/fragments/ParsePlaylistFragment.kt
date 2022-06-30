package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.EndlessRecyclerViewScrollListener
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.adapters.SongAdapter
import com.dev.moosic.adapters.TopTrackAdapter
import com.dev.moosic.models.Playlist
import com.dev.moosic.models.Song
import com.parse.ParseObject
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_PARAM1 = "playlistSongs"
private const val ARG_PARAM2 = "buttonsToShow"
private const val ARG_PARAM3 = "playlistObject"

/**
 * A simple [Fragment] subclass.
 * Use the [ParsePlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class ParsePlaylistFragment(controller : MainActivity.MainActivityController) : Fragment() {
    private var songs: ArrayList<Song> = ArrayList()
    private var mainActivityController = controller
    private var buttonsToShow: List<String> = ArrayList()
//    private var playlistObject: ParseObject? = null

    var rvPlaylistTracks : RecyclerView? = null
    var adapter : SongAdapter? = null
    var scrollListener : EndlessRecyclerViewScrollListener? = null
    var swipeContainer: SwipeRefreshLayout? = null

    var playlistTitle: TextView? = null
    var playlistDescription: TextView? = null
    var playlistAuthor: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            songs.clear()
            songs = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            buttonsToShow = it.getStringArrayList(ARG_PARAM2)!!
//            playlistObject = Parcels.unwrap(it.getParcelable(ARG_PARAM3))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_library, container, false)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(playlistSongs: ArrayList<Song>, controller: MainActivity.MainActivityController,
                        buttonsToShow: ArrayList<String>/*, playlistObject: Playlist*/) =
            ParsePlaylistFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(playlistSongs))
                    putStringArrayList(ARG_PARAM2, buttonsToShow)
//                    putParcelable(ARG_PARAM3, Parcels.wrap(playlistObject))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide for now
        playlistTitle = view.findViewById(R.id.playlistTitle)
        playlistAuthor = view.findViewById(R.id.playlistAuthor)
        playlistDescription = view.findViewById(R.id.playlistDescription)
        listOf(playlistTitle, playlistAuthor, playlistDescription).map{ tv -> tv?.visibility = View.GONE }

//        val titleText = playlistObject?.getName()
//        val descriptionText = playlistObject?.getDescription()
//        val author : ParseUser? = playlistObject?.getAuthor()
//        val authorText = author?.username
//
//        if (titleText != null){
//            playlistTitle?.setText(titleText)
//        } else {
//            playlistTitle?.setText("Add a title to this playlist")
//        }
//
//        if (descriptionText != null){
//            playlistDescription?.setText(descriptionText)
//        } else {
//            playlistDescription?.visibility = View.GONE
//        }
//
//        if (authorText != null){
//            playlistAuthor?.setText(authorText)
//        } else {
//            playlistAuthor?.visibility = View.GONE
//        }

        rvPlaylistTracks = view.findViewById(R.id.rvPlaylistTracks)

        Log.d("ParsePlaylistFragment", "songs in parse playlist fragment: " + songs.size)
        adapter = SongAdapter(view.context,
            songs, mainActivityController, buttonsToShow) // TODO: replace w list..

        rvPlaylistTracks?.adapter = adapter
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
//        scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
//            override fun onLoadMore(offset: Int, totalItemsCount: Int, view: RecyclerView?) {
//                mainActivityController.loadMorePlaylistSongs(tracks.size, totalItemsCount, adapter!!)
//            }
//        })
//
//        rvPlaylistTracks?.addOnScrollListener(scrollListener!!);



    }
}