package com.dev.moosic.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.adapters.TopTrackAdapter
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.Playlist
import kaaes.spotify.webapi.android.models.Track
import kaaes.spotify.webapi.android.models.UserPrivate
import org.parceler.Parcels

private const val ARG_PARAM1 = "topTracks"
private const val ARG_PARAM2 = "currentUser"
private const val ARG_PARAM3 = "userPlaylist"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFeedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFeedFragment(controller : MainActivity.MainActivityController) : Fragment() {
    // TODO: Rename and change types of parameters
    private var topTracks: ArrayList<Track> = ArrayList()
    private var currentUserId : String? = null
    private var userPlaylistId : String? = null
    private var mainActivityController : MainActivity.MainActivityController = controller

    val TAG = "HomeFeedFragment"

    var rvTopTracks : RecyclerView? = null
    var adapter : TopTrackAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            topTracks.clear()
            topTracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            currentUserId = it.getString(ARG_PARAM2);
            userPlaylistId = it.getString(ARG_PARAM3)
//            mainActivityController = Parcels.unwrap(it.getParcelable(ARG_PARAM4))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_feed, container, false)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(tracks: ArrayList<Track>, userId: String, playlistId: String,
        controller : MainActivity.MainActivityController) =
            HomeFeedFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(tracks))
                    putString(ARG_PARAM2, userId)
                    putString(ARG_PARAM3, playlistId)
//                    putParcelable(ARG_PARAM4, Parcels.wrap(controller))
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTopTracks = view.findViewById(R.id.rvTopTracks)

        adapter = TopTrackAdapter(view.context, topTracks, currentUserId!!, userPlaylistId!!,
        mainActivityController, true, false)

        rvTopTracks?.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(context)
        rvTopTracks?.setLayoutManager(linearLayoutManager)
    }
}