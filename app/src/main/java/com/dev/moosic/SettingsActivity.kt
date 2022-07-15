package com.dev.moosic

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.parse.ParseUser

private const val TOAST_FAILED_LOGOUT = "Failed to log out"

class SettingsActivity() : AppCompatActivity() {
    lateinit var username : TextView
    private lateinit var spotifyAccount : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var logOutButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        username = findViewById(R.id.settingsUsername)
        spotifyAccount = findViewById(R.id.settingsSpotifyAccountUsername)
        email = findViewById(R.id.settingsEmail)
        phoneNumber = findViewById(R.id.settingsPhoneNumber)
        logOutButton = findViewById(R.id.settingsLogOutButton)

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
            ParseUser.logOutInBackground {
                if (it != null) {
                    Toast.makeText(this,
                        "$TOAST_FAILED_LOGOUT: ${it.message}", Toast.LENGTH_LONG).show()
                }
                else {
                    setResult(Util.RESULT_CODE_LOG_OUT)
                    finish()
                }
            }
        }
        // backButton.setOnClickListener
        // -- set a result code as having not logged out, then finish myself

    }
}