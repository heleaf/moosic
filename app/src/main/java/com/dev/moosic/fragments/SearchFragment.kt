package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dev.moosic.EndlessRecyclerViewScrollListener
import com.dev.moosic.LoadMoreFunction
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.adapters.TrackAdapter
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_PARAM1 = "searchedTracks"
private const val ARG_PARAM2 = "userId"
private const val ARG_PARAM3 = "playlistId"
private const val ARG_PARAM4 = "searchedQuery"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment(controller: MainActivity.MainActivitySongController) : Fragment() {
    private var searchedTracks: ArrayList<Track> = ArrayList()
    private var userId : String? = null
    private var playlistId : String? = null
    private var mainActivityController = controller
    private var currentQuery : String? = null

    var rvSearchedTracks : RecyclerView? = null
    var adapter : TrackAdapter? = null // TODO: switch to a custom adapter
    var scrollListener : EndlessRecyclerViewScrollListener? = null

    var swipeContainer : SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchedTracks.clear()
            searchedTracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            userId = it.getString(ARG_PARAM2)
            playlistId = it.getString(ARG_PARAM3)
            currentQuery = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(searchedTracks : ArrayList<Track>, userId : String,
                        playlistId : String, controller : MainActivity.MainActivitySongController,
                        searchedQuery: String) =
            SearchFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(searchedTracks))
                    putString(ARG_PARAM2, userId)
                    putString(ARG_PARAM3, playlistId)
                    putString(ARG_PARAM4, searchedQuery)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvSearchedTracks = view.findViewById(R.id.rvSearchedTracks)
        adapter = TrackAdapter(view.context, searchedTracks,
            userId!!, playlistId!!, mainActivityController, true, false)

        rvSearchedTracks?.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvSearchedTracks?.layoutManager = linearLayoutManager

        if (currentQuery != null){
            scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    mainActivityController.loadMoreSearchTracks(currentQuery!!, searchedTracks.size, totalItemsCount, adapter!!)
                }
            })
            rvSearchedTracks?.addOnScrollListener(scrollListener!!);
        }

        swipeContainer = view.findViewById(R.id.searchFeedSwipeContainer)
        swipeContainer?.setOnRefreshListener {
//            mainActivityController.loadMoreSearchTracks(
//                currentQuery!!,
//                0, 20, adapter!!)
//            scrollListener?.resetState()
            swipeContainer?.isRefreshing = false
        }

        swipeContainer?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )


    }
}