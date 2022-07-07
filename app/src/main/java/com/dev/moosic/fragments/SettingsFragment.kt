package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dev.moosic.AuthorizationController
import com.dev.moosic.MainActivity
import com.dev.moosic.PlaylistController
import com.dev.moosic.R
import com.parse.ParseUser

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment(controller: PlaylistController) : Fragment() {
    // TODO: Rename and change types of parameters
    private var settingsController = controller

    private val KEY_PHONE_NUMBER = "phoneNumber"
    private val KEY_SPOTIFY_ACCOUNT = "userId"

    var username : TextView? = null
    var spotifyAccount : TextView? = null
    var email : TextView? = null
    var phoneNumber : TextView? = null
    var logOutButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance(controller: PlaylistController) =
            SettingsFragment(controller).apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        username = view.findViewById(R.id.settingsUsername)
        spotifyAccount = view.findViewById(R.id.settingsSpotifyAccountUsername)
        email = view.findViewById(R.id.settingsEmail)
        phoneNumber = view.findViewById(R.id.settingsPhoneNumber)
        logOutButton = view.findViewById(R.id.settingsLogOutButton)

        val currentUser = ParseUser.getCurrentUser()
        val usernameText = currentUser.username
        val emailText = currentUser.email
        val phoneNumberText = currentUser.getString(KEY_PHONE_NUMBER)
        val spotifyAccountText = currentUser.getString(KEY_SPOTIFY_ACCOUNT)

        username?.setText(usernameText)
        spotifyAccount?.setText(spotifyAccountText)
        email?.setText(emailText)
        phoneNumber?.setText(phoneNumberText)

        logOutButton?.setOnClickListener {
            settingsController.logOut()
        }

    }
}