package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.dev.moosic.controllers.SpotifyAuthController
import com.dev.moosic.models.Playlist
import com.google.gson.Gson
import com.parse.ParseUser
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.parceler.Parcels

private const val TAG = "SignUpActivity"
private const val TOAST_INVALID_PHONE_NUMBER = "Invalid phone number inputted"
private const val TOAST_SIGNUP_ERROR = "Error signing up: "
private const val TOAST_SIGNUP_SUCCESS = "Successfully signed up"
private const val TOAST_SPOTIFY_AUTH_FAILURE = "Failed to authorize spotify account, " +
        "please restart the app to try again"

class SignUpActivity : AppCompatActivity() {
    lateinit var mUsername : EditText
    lateinit var mPassword : EditText
    lateinit var mEmail : EditText
    lateinit var mPhoneNumber : EditText
    lateinit var mSignUpButton : Button
    lateinit var accessToken: String
    var user : ParseUser = ParseUser()

    lateinit var showHidePasswordButton: ImageButton
    var showingPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mUsername = findViewById(R.id.etSignUpUsername)
        mPassword = findViewById(R.id.etSignUpPassword)
        showHidePasswordButton = findViewById(R.id.showHidePasswordButton)
        mEmail = findViewById(R.id.etSignUpEmail)
        mPhoneNumber = findViewById(R.id.etSignUpPhone)
        mSignUpButton = findViewById(R.id.signUpSignUpButton)

        showHidePasswordButton.setOnClickListener {
            if (showingPassword) {
                mPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                mPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            showingPassword = !showingPassword
        }

        mSignUpButton.setOnClickListener(View.OnClickListener {
            val usernameText = mUsername.text
            val passwordText = mPassword.text
            val emailText = mEmail.text
            val phoneText = mPhoneNumber.text
            if (!(usernameText == null || passwordText == null ||
                    emailText == null || phoneText == null)) {
                signUp(usernameText.toString(), passwordText.toString(),
                    emailText.toString(), phoneText.toString())
            }

        })

        accessToken = intent.getStringExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN).toString()
        val usernameText = intent.getStringExtra(Util.INTENT_KEY_USERNAME_TEXT)
        val passwordText = intent.getStringExtra(Util.INTENT_KEY_PASSWORD_TEXT)

        if (usernameText != null){
            mUsername.setText(usernameText)
        }

        if (passwordText != null){
            mPassword.setText(passwordText)
        }

    }

    private fun signUp(username: String, password: String, email: String, phone: String) {
        user.setUsername(username)
        user.setPassword(password)
        user.setEmail(email)

        if (!PhoneNumberUtils.isGlobalPhoneNumber(phone)){
            Toast.makeText(this, TOAST_INVALID_PHONE_NUMBER,
                Toast.LENGTH_LONG).show()
            return
        }

        user.put(Util.PARSEUSER_KEY_PHONE_NUMBER, phone)

        user.signUpInBackground {
            if (it != null){
                Toast.makeText(this,
                TOAST_SIGNUP_ERROR + it.message,
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,
                TOAST_SIGNUP_SUCCESS, Toast.LENGTH_SHORT).show()
                val playlist = Playlist()
                playlist.saveInBackground { error ->
                    if (error != null) {
                        error.message?.let { it1 -> Log.e(TAG, it1) }
                    }
                    else {
                        user.put(Util.PARSEUSER_KEY_PARSE_PLAYLIST, playlist)
                        user.saveInBackground()
                        SpotifyAuthController(this).authorizeUser()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SpotifyAuthController(this).AUTH_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    accessToken = response.accessToken
                    val intent = Intent(this, GetInterestsActivity::class.java)
                    intent.putExtra(Util.INTENT_KEY_NEW_USER, Parcels.wrap(user))
                    intent.putExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN, accessToken)
                    startActivityForResult(intent, Util.REQUEST_CODE_GET_INTERESTS)
                }
                AuthorizationResponse.Type.ERROR -> {
                    Toast.makeText(this, TOAST_SPOTIFY_AUTH_FAILURE + response.error,
                        Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, TOAST_SPOTIFY_AUTH_FAILURE,
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        if (requestCode == Util.REQUEST_CODE_GET_INTERESTS) {
            if (resultCode == RESULT_OK){
                val genres : ArrayList<String>
                    = Parcels.unwrap(data?.getParcelableExtra(Util.INTENT_KEY_USER_PICKED_GENRES))
                val gson = Gson()
                val pickedGenresJsonString = gson.toJson(genres)
                user.put(Util.PARSEUSER_KEY_FAVORITE_GENRES, pickedGenresJsonString)
                user.saveInBackground {
                    if (it != null) {
                        it.message?.let { it1 -> Log.e(TAG, it1) }
                        return@saveInBackground
                    }
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}