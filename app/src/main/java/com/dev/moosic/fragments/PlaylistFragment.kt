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
import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

// currently not in use

private const val ARG_PARAM1 = "playlistTracks"
private const val ARG_PARAM2 = "currentUserId"
private const val ARG_PARAM3 = "userPlaylistId"
private const val ARG_PARAM4 = "showAddButton"
private const val ARG_PARAM5 = "showDeleteButton"
private const val ARG_PARAM6 = "showHeartButton"

/**
 * A simple [Fragment] subclass.
 * Use the [ParsePlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class PlaylistFragment(controller : MainActivity.MainActivityController) : Fragment() {
    val TAG = "PlaylistFragment"

    private var tracks: ArrayList<Track> = ArrayList()
    private var currentUserId: String? = null
    private var userPlaylistId: String? = null
    private var mainActivityController = controller
    private var showAddButton: Boolean? = null
    private var showDeleteButton: Boolean? = null
    private var showHeartButton: Boolean? = null

    var rvPlaylistTracks : RecyclerView? = null
    var adapter : TopTrackAdapter? = null // TODO: switch to a different track adapter
    var scrollListener : EndlessRecyclerViewScrollListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tracks.clear()
            tracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            currentUserId = it.getString(ARG_PARAM2)
            userPlaylistId = it.getString(ARG_PARAM3)
            showAddButton = it.getBoolean(ARG_PARAM4)
            showDeleteButton = it.getBoolean(ARG_PARAM5)
            showHeartButton = it.getBoolean(ARG_PARAM6)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_library, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(playlistTracks: ArrayList<Track>, userId: String, playlistId: String,
                        controller: MainActivity.MainActivityController,
                        showAdd: Boolean, showDelete: Boolean, showHeart: Boolean) =
            ParsePlaylistFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(playlistTracks))
                    putString(ARG_PARAM2, userId)
                    putString(ARG_PARAM3, playlistId)
                    putBoolean(ARG_PARAM4, showAdd)
                    putBoolean(ARG_PARAM5, showDelete)
                    putBoolean(ARG_PARAM6, showHeart)
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
        Log.d(TAG, "curent user id: " + currentUserId)
        Log.d(TAG, "user playlist id: " + userPlaylistId)
        adapter = TopTrackAdapter(view.context,
            tracks,
            currentUserId!!, userPlaylistId!!, mainActivityController,
            showAddButton!!, showDeleteButton!!)
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