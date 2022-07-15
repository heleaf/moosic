package com.dev.moosic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.moosic.controllers.SpotifyAuthController
import com.parse.LogInCallback
import com.parse.ParseUser
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse

private const val TAG = "LoginActivity"
private const val LOGGED_OUT_REQUEST_CODE = 1338

private const val TOAST_USERNAME_EMPTY = "username cannot be empty"
private const val TOAST_PASSWORD_EMPTY = "password cannot be empty"
private const val TOAST_LOGIN_ISSUE = "issue with login: "

private const val DEFAULT_USERNAME_TEXT = ""
private const val DEFAULT_PASSWORD_TEXT = ""

class LoginActivity : AppCompatActivity() {
    lateinit var mEtUsername : EditText
    lateinit var mEtPassword : EditText
    lateinit var mLoginButton : Button
    lateinit var mSignUpButton : Button
    var accessToken : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mEtUsername = findViewById(R.id.etLoginUsername)
        mEtPassword = findViewById(R.id.etLoginPassword)
        mLoginButton = findViewById(R.id.loginLoginButton)
        mSignUpButton = findViewById(R.id.loginSignUpButton)

        mLoginButton.setOnClickListener(View.OnClickListener {
            val usernameText = mEtUsername.text;
            val passwordText = mEtPassword.text;
            if (usernameText.toString().isEmpty()){
                Toast.makeText(this, TOAST_USERNAME_EMPTY, Toast.LENGTH_LONG).show()
            } else if (passwordText.toString().isEmpty()){
                Toast.makeText(this, TOAST_PASSWORD_EMPTY, Toast.LENGTH_LONG).show()
            }
            else logInUser(usernameText.toString(), passwordText.toString())
        })

        mSignUpButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            val usernameText = mEtUsername.text.toString();
            val passwordText = mEtPassword.text.toString();
            intent.putExtra(Util.INTENT_KEY_USERNAME_TEXT, usernameText)
            intent.putExtra(Util.INTENT_KEY_PASSWORD_TEXT, passwordText)
            intent.putExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN, accessToken)
            startActivity(intent)
        })

    }

    override fun onStart() {
        super.onStart()
        if (ParseUser.getCurrentUser() != null){
            // authorize
            SpotifyAuthController(this).authorizeUser()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SpotifyAuthController(this).AUTH_REQUEST_CODE) {
        val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    accessToken = response.accessToken
                    if (ParseUser.getCurrentUser() != null) {
                        goMainActivity();
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e(TAG, response.error)
                }
                else -> { Log.d(TAG, response.type.toString()) }
            }
        } else if (requestCode == LOGGED_OUT_REQUEST_CODE) {
            mEtUsername.setText(DEFAULT_USERNAME_TEXT)
            mEtPassword.setText(DEFAULT_PASSWORD_TEXT)
        }
    }

    private fun logInUser(usernameText: String, passwordText: String) {
        ParseUser.logInInBackground(usernameText, passwordText, LogInCallback { user, e ->
            if (e != null){
                Toast.makeText(this, TOAST_LOGIN_ISSUE + e.message,
                    Toast.LENGTH_LONG).show();
            }
            else {
                SpotifyAuthController(this).authorizeUser()
            }
        })
    }

    private fun goMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN, accessToken)
        startActivityForResult(intent, LOGGED_OUT_REQUEST_CODE)
    }

}