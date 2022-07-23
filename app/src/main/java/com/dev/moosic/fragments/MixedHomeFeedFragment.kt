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
import com.dev.moosic.R
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

private const val ARG_MIXED_ITEM_LIST = "mixedItemList"
private const val ARG_TOP_TRACKS_LIST = "topTracksList"

private const val TAG = "MixedHomeFeedFragment"

class MixedHomeFeedFragment(private val mainActivitySongController: MainActivityControllerInterface,
                            private val playlistController: UserRepoPlaylistControllerInterface) : Fragment() {
    private var mixedItemList : ArrayList<Pair<Any, String>> = ArrayList()
    private var topTracksList : ArrayList<Track> = ArrayList()
    private lateinit var mixedItemListRv : RecyclerView
    lateinit var adapter : HomeFeedItemAdapter
    lateinit var scrollListener : EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mixedItemList = Parcels.unwrap(it.getParcelable(ARG_MIXED_ITEM_LIST))
            topTracksList = Parcels.unwrap(it.getParcelable(ARG_TOP_TRACKS_LIST))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mixed_home_feed, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(mixedItemList: ArrayList<Pair<Any, String>>, topTracks: ArrayList<Track>,
                        mainActivitySongController: MainActivityControllerInterface, playlistController: UserRepoPlaylistControllerInterface) =
            MixedHomeFeedFragment(mainActivitySongController, playlistController).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MIXED_ITEM_LIST, Parcels.wrap(mixedItemList))
                    putParcelable(ARG_TOP_TRACKS_LIST, Parcels.wrap(topTracks))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mixedItemListRv = view.findViewById(R.id.mixedItemsRv)
        adapter = HomeFeedItemAdapter(view.context, mixedItemList, mainActivitySongController, playlistController)

        mixedItemListRv.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        mixedItemListRv.layoutManager = linearLayoutManager

        scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager,
            object: LoadMoreFunction {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                mainActivitySongController.loadMoreMixedHomeFeedItems(topTracksList.size,
                    totalItemsCount, adapter, null)
            }
        })

        mixedItemListRv.addOnScrollListener(scrollListener)

        val swipeRefreshLayout : SwipeRefreshLayout = view.findViewById(R.id.homeFeedSwipeContainer)
        swipeRefreshLayout.setOnRefreshListener {
            mainActivitySongController.resetHomeFragment(swipeRefreshLayout)
        }
    }
}