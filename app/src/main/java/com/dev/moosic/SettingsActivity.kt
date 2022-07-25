package com.dev.moosic

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dev.moosic.controllers.SettingsController
import com.dev.moosic.controllers.SettingsControllerInterface
import com.dev.moosic.fragments.SettingsFragment
import com.parse.ParseUser

class SettingsActivity() : AppCompatActivity() {
//    lateinit var username : TextView
//    private lateinit var spotifyAccount : TextView
//    private lateinit var email : TextView
//    private lateinit var phoneNumber : TextView
//    private lateinit var logOutButton : Button
//    private lateinit var loadingBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            setResult(Util.RESULT_CODE_EXIT_SETTINGS)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        val settingsFragment = SettingsFragment.newInstance(SettingsController(this))
        supportFragmentManager
            .beginTransaction().replace(R.id.settingsFlContainer, settingsFragment).commit()


//        username = findViewById(R.id.settingsUsername)
//        spotifyAccount = findViewById(R.id.settingsSpotifyAccountUsername)
//        email = findViewById(R.id.settingsEmail)
//        phoneNumber = findViewById(R.id.settingsPhoneNumber)
//        logOutButton = findViewById(R.id.settingsLogOutButton)
//
//        val currentUser = ParseUser.getCurrentUser()
//        val usernameText = currentUser.username
//        val emailText = currentUser.email
//        val phoneNumberText = currentUser.getString(Util.PARSEUSER_KEY_PHONE_NUMBER)
//        val spotifyAccountText = currentUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME)
//
//        username.setText(usernameText)
//        spotifyAccount.setText(spotifyAccountText)
//        email.setText(emailText)
//        phoneNumber.setText(phoneNumberText)
//
//        loadingBar = findViewById(R.id.settingsLoadingBar)
//
//        logOutButton.setOnClickListener {
//            showProgressBar()
//            ParseUser.logOutInBackground {
//                if (it != null) {
//                    Toast.makeText(this,
//                        "$TOAST_FAILED_LOGOUT: ${it.message}", Toast.LENGTH_LONG).show()
//                    hideProgressBar()
//                }
//                else {
//                    setResult(Util.RESULT_CODE_LOG_OUT)
//                    hideProgressBar()
//                    finish()
//                }
//            }
//        }
//
//        hideProgressBar()

    }

//    private fun hideProgressBar() {
//        loadingBar.visibility = View.INVISIBLE
//    }
//
//    private fun showProgressBar() {
//        loadingBar.visibility = View.VISIBLE
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}