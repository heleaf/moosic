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
import com.dev.moosic.EndlessRecyclerViewScrollListener
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.adapters.SongAdapter
import com.dev.moosic.adapters.TopTrackAdapter
import com.dev.moosic.models.Song
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_PARAM1 = "playlistSongs"
private const val ARG_PARAM2 = "buttonsToShow"

/**
 * A simple [Fragment] subclass.
 * Use the [ParsePlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class ParsePlaylistFragment(controller : MainActivity.MainActivityController) : Fragment() {

    private var songs: ArrayList<Song> = ArrayList()
    private var mainActivityController = controller
    private var buttonsToShow: List<String> = ArrayList()

    var rvPlaylistTracks : RecyclerView? = null
    var adapter : SongAdapter? = null
    var scrollListener : EndlessRecyclerViewScrollListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songs.clear()
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(playlistSongs: ArrayList<Song>, controller: MainActivity.MainActivityController,
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
        // Hide for now
        val title : TextView = view.findViewById(R.id.playlistTitle)
        val author : TextView = view.findViewById(R.id.playlistAuthor)
        val descrip : TextView = view.findViewById(R.id.playlistDescription)
        listOf(title, author, descrip).map{ tv -> tv.visibility = View.GONE }
        rvPlaylistTracks = view.findViewById(R.id.rvPlaylistTracks)

        adapter = SongAdapter(view.context,
            songs, mainActivityController, buttonsToShow) // TODO: replace w list..

        rvPlaylistTracks?.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvPlaylistTracks?.setLayoutManager(linearLayoutManager)

//        scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
//            override fun onLoadMore(offset: Int, totalItemsCount: Int, view: RecyclerView?) {
//                mainActivityController.loadMorePlaylistSongs(tracks.size, totalItemsCount, adapter!!)
//            }
//        })
//
//        rvPlaylistTracks?.addOnScrollListener(scrollListener!!);

    }
}