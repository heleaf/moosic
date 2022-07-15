package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dev.moosic.controllers.SongController
import com.dev.moosic.R
import com.dev.moosic.Util
import com.parse.ParseUser

class SettingsFragment(controller: SongController) : Fragment() {
    private var settingsController = controller
    lateinit var username : TextView
    private lateinit var spotifyAccount : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var logOutButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(controller: SongController) =
            SettingsFragment(controller).apply {
                arguments = Bundle().apply {}
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
        val phoneNumberText = currentUser.getString(Util.PARSEUSER_KEY_PHONE_NUMBER)
        val spotifyAccountText = currentUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME)

        username.setText(usernameText)
        spotifyAccount.setText(spotifyAccountText)
        email.setText(emailText)
        phoneNumber.setText(phoneNumberText)

        logOutButton.setOnClickListener {
            settingsController.logOutFromParse()
        }

    }
}