package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "topTracks"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFeedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var topTracks: ArrayList<Track> = ArrayList()
    val TAG = "HomeFeedFragment"

    var rvTopTracks : RecyclerView? = null
    var adapter : TopTrackAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            topTracks.clear()
            topTracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            Log.d(TAG, topTracks.size.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_feed, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment HomeFeedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(topTracks: ArrayList<Track>) =
            HomeFeedFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(topTracks))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTopTracks = view.findViewById(R.id.rvTopTracks)

        adapter = TopTrackAdapter(view.context, topTracks)

        rvTopTracks?.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvTopTracks?.setLayoutManager(linearLayoutManager)
    }
}