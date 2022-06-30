package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.EndlessRecyclerViewScrollListener
import com.dev.moosic.LoadMoreFunction
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "searchedTracks"
private const val ARG_PARAM2 = "userId"
private const val ARG_PARAM3 = "playlistId"
private const val ARG_PARAM4 = "searchedQuery"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment(controller: MainActivity.MainActivityController) : Fragment() {
    // TODO: Rename and change types of parameters
    private var searchedTracks: ArrayList<Track> = ArrayList()
    private var userId : String? = null
    private var playlistId : String? = null
    private var mainActivityController = controller
    private var currentQuery : String? = null

    var rvSearchedTracks : RecyclerView? = null
    var adapter : TopTrackAdapter? = null // TODO: switch to a custom adapter
    var scrollListener : EndlessRecyclerViewScrollListener? = null

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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(searchedTracks : ArrayList<Track>, userId : String,
        playlistId : String, controller : MainActivity.MainActivityController,
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
        adapter = TopTrackAdapter(view.context, searchedTracks,
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


    }
}