package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import kaaes.spotify.webapi.android.models.PlaylistTrack
import org.parceler.Parcels

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "playlistTracks"
private const val ARG_PARAM2 = "currentUserId"
private const val ARG_PARAM3 = "userPlaylistId"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileLibraryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileLibraryFragment(controller : MainActivity.MainActivityController) : Fragment() {
    // TODO: Rename and change types of parameters
    private var playlistTracks: ArrayList<PlaylistTrack> = ArrayList()
    private var currentUserId: String? = null
    private var userPlaylistId: String? = null
    private var mainActivityController = controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistTracks = Parcels.unwrap(it.getParcelable(ARG_PARAM1))
            currentUserId = it.getString(ARG_PARAM2)
            userPlaylistId = it.getString(ARG_PARAM3)
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(playlistTracks: ArrayList<PlaylistTrack>, userId: String, playlistId: String,
                        controller: MainActivity.MainActivityController) =
            ProfileLibraryFragment(controller).apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, Parcels.wrap(playlistTracks))
                    putString(ARG_PARAM2, userId)
                    putString(ARG_PARAM2, playlistId)
                }
            }
    }
}