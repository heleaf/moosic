package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.EndlessRecyclerViewScrollListener
import com.dev.moosic.LoadMoreFunction
import com.dev.moosic.R
import com.dev.moosic.adapters.HomeFeedItemAdapter
import com.dev.moosic.controllers.SongController
import org.parceler.Parcels
import kotlin.properties.Delegates

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val KEY_MIXED_ITEM_LIST = "mixedItemList"
private const val KEY_NUMBER_FRIENDS_PLAYLISTS_INSERTED = "numberFriendsPlaylistsInserted"

/**
 * A simple [Fragment] subclass.
 * Use the [MixedHomeFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class MixedHomeFeedFragment(private var songController: SongController) : Fragment() {
    // TODO: Rename and change types of parameters
    private var mixedItemList : ArrayList<Pair<Any, String>> = ArrayList()

    var numberFriendsPlaylistsInserted by Delegates.notNull<Int>()

    lateinit var mixedItemListRv : RecyclerView
    lateinit var adapter : HomeFeedItemAdapter
    lateinit var scrollListener : EndlessRecyclerViewScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mixedItemList = Parcels.unwrap(it.getParcelable(KEY_MIXED_ITEM_LIST))
            numberFriendsPlaylistsInserted = it.getInt(KEY_NUMBER_FRIENDS_PLAYLISTS_INSERTED)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mixed_home_feed, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment MixedHomeFeedFragment.
         */
        @JvmStatic
        fun newInstance(mixedItemList: ArrayList<Pair<Any, String>>, numberFriendsPlaylistInserted: Int, songController: SongController) =
            MixedHomeFeedFragment(songController).apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_MIXED_ITEM_LIST, Parcels.wrap(mixedItemList))
                    putInt(KEY_NUMBER_FRIENDS_PLAYLISTS_INSERTED, numberFriendsPlaylistInserted)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mixedItemListRv = view.findViewById(R.id.mixedItemsRv)
        adapter = HomeFeedItemAdapter(view.context, mixedItemList, songController)

        mixedItemListRv.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        mixedItemListRv.setLayoutManager(linearLayoutManager)

        scrollListener = EndlessRecyclerViewScrollListener(linearLayoutManager, object: LoadMoreFunction {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                Log.d("MixedHomeFeedFragment", "loading: " + totalItemsCount)
                songController.loadMoreMixedHomeFeedItems(totalItemsCount, adapter, null)
            }
        })

        mixedItemListRv.addOnScrollListener(scrollListener)

    }
}