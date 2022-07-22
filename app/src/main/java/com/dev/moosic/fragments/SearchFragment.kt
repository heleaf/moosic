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

private const val ARG_SEARCHED_TRACKS = "searchedTracks"
private const val ARG_SEARCHED_QUERY_STR = "searchedQuery"

class SearchFragment(controller: MainActivity.MainActivitySongController) : Fragment() {
    private var searchedTracks: ArrayList<Track> = ArrayList()
    private var mainActivityController = controller
    private lateinit var currentQuery : String

    lateinit var rvSearchedTracks : RecyclerView
    lateinit var adapter : TrackAdapter
    lateinit var scrollListener : EndlessRecyclerViewScrollListener

    lateinit var swipeContainer : SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchedTracks.clear()
            searchedTracks = Parcels.unwrap(it.getParcelable(ARG_SEARCHED_TRACKS))
            currentQuery = if (it.getString(ARG_SEARCHED_QUERY_STR) == null)
                "" else it.getString(ARG_SEARCHED_QUERY_STR).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(searchedTracks : ArrayList<Track>, searchedQuery: String,
                        controller : MainActivity.MainActivitySongController) =
            SearchFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_SEARCHED_TRACKS, Parcels.wrap(searchedTracks))
                    putString(ARG_SEARCHED_QUERY_STR, searchedQuery)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvSearchedTracks = view.findViewById(R.id.rvSearchedTracks)
        adapter = TrackAdapter(view.context, searchedTracks,
            mainActivityController, true, false)

        rvSearchedTracks.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvSearchedTracks.layoutManager = linearLayoutManager

        if (currentQuery.isNotEmpty()){
            scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager,
                object: LoadMoreFunction {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    mainActivityController.loadMoreSearchTracks(currentQuery, searchedTracks.size,
                        totalItemsCount, adapter)
                }
            })
            rvSearchedTracks.addOnScrollListener(scrollListener);
        }

        swipeContainer = view.findViewById(R.id.searchFeedSwipeContainer)
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