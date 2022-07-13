package com.dev.moosic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

private const val ARG_PARAM1 = "topTracks"
private const val ARG_PARAM2 = "currentUser"
private const val ARG_PARAM3 = "userPlaylist"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

open class HomeFeedFragment(controller : MainActivity.MainActivitySongController) : Fragment() {
    var topTracks: ArrayList<Track> = ArrayList()
    private var currentUserId : String? = null
    private var userPlaylistId : String? = null
    private var mainActivitySongController : MainActivity.MainActivitySongController = controller

    val TAG = "HomeFeedFragment"

    var rvTopTracks : RecyclerView? = null
    var adapter : TrackAdapter? = null
    var scrollListener: EndlessRecyclerViewScrollListener? = null
    var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            topTracks.clear()
            topTracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            currentUserId = it.getString(ARG_PARAM2);
            userPlaylistId = it.getString(ARG_PARAM3)
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
        fun newInstance(tracks: ArrayList<Track>, userId: String, playlistId: String,
        controller : MainActivity.MainActivitySongController) =
            HomeFeedFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(tracks))
                    putString(ARG_PARAM2, userId)
                    putString(ARG_PARAM3, playlistId)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTopTracks = view.findViewById(R.id.rvTopTracks)

        if (currentUserId != null) {
            adapter = TrackAdapter(view.context, topTracks, currentUserId!!, userPlaylistId!!,
                mainActivitySongController, true, false)

            rvTopTracks?.adapter = adapter
            val linearLayoutManager = LinearLayoutManager(context)
            rvTopTracks?.setLayoutManager(linearLayoutManager)

            scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
                override fun onLoadMore(offset: Int, totalItemsCount: Int, view: RecyclerView?) {
                    mainActivitySongController.loadMoreTopSongs(topTracks.size, totalItemsCount, false, adapter!!, swipeContainer!!)
                }
            })

            rvTopTracks?.addOnScrollListener(scrollListener!!);

            swipeContainer = view.findViewById(R.id.homeFeedSwipeContainer)
            swipeContainer?.setOnRefreshListener {
                mainActivitySongController.loadMoreTopSongs(0, 20, true, adapter!!,
                    swipeContainer!!)
                scrollListener?.resetState()
            }

            swipeContainer?.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )
        }
    }
}