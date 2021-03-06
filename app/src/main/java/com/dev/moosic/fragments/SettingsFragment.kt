package com.dev.moosic.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.dev.moosic.R
import com.dev.moosic.Util
import com.dev.moosic.controllers.SettingsControllerInterface
import com.parse.ParseUser

class SettingsFragment(private val settingsControllerInterface: SettingsControllerInterface) : Fragment() {
    lateinit var username : TextView
    private lateinit var spotifyAccount : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var logOutButton : Button
    private lateinit var loadingBar : ProgressBar


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
        fun newInstance(settingsControllerInterface: SettingsControllerInterface) =
            SettingsFragment(settingsControllerInterface).apply {
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

        loadingBar = view.findViewById(R.id.settingsLoadingBar)

        logOutButton.setOnClickListener {
            showProgressBar()
            ParseUser.logOutInBackground {
                if (it != null) {
                    Toast.makeText(view.context,
                        "${Util.TOAST_FAILED_LOGOUT}: ${it.message}",
                        Toast.LENGTH_LONG).show()
                    hideProgressBar()
                }
                else {
                    settingsControllerInterface.logOut(loadingBar)
                }
            }
        }
        hideProgressBar()
    }

    private fun hideProgressBar() {
        loadingBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        loadingBar.visibility = View.VISIBLE
    }
}