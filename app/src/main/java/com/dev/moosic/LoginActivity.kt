package com.dev.moosic

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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

private const val TOAST_USERNAME_EMPTY = "Username cannot be empty"
private const val TOAST_PASSWORD_EMPTY = "Password cannot be empty"
private const val TOAST_LOGIN_ISSUE = "Issue with login: "
private const val TOAST_NOT_CONNECTED_TO_INTERNET = "Cannot log in, no Internet connection found"

private const val DEFAULT_USERNAME_TEXT = ""
private const val DEFAULT_PASSWORD_TEXT = ""

class LoginActivity : AppCompatActivity() {
    lateinit var etUsername : EditText
    lateinit var etPassword : EditText
    lateinit var etLoginButton : Button
    lateinit var etSignupButton : Button
    var accessToken : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etLoginUsername)
        etPassword = findViewById(R.id.etLoginPassword)
        etLoginButton = findViewById(R.id.loginLoginButton)
        etSignupButton = findViewById(R.id.loginSignUpButton)

        etLoginButton.setOnClickListener(View.OnClickListener {
            val usernameText = etUsername.text;
            val passwordText = etPassword.text;
            if (usernameText.toString().isEmpty()){
                Toast.makeText(this, TOAST_USERNAME_EMPTY, Toast.LENGTH_LONG).show()
            } else if (passwordText.toString().isEmpty()){
                Toast.makeText(this, TOAST_PASSWORD_EMPTY, Toast.LENGTH_LONG).show()
            }
            else logInUser(usernameText.toString(), passwordText.toString())
        })

        etSignupButton.setOnClickListener(View.OnClickListener {
            if (!isConnectedToInternet(this)) {
                Toast.makeText(this, TOAST_NOT_CONNECTED_TO_INTERNET, Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            val intent = Intent(this, SignUpActivity::class.java)
            val usernameText = etUsername.text.toString();
            val passwordText = etPassword.text.toString();
            intent.putExtra(Util.INTENT_KEY_USERNAME_TEXT, usernameText)
            intent.putExtra(Util.INTENT_KEY_PASSWORD_TEXT, passwordText)
            intent.putExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN, accessToken)
            startActivity(intent)
        })
    }

    override fun onStart() {
        super.onStart()
        if (ParseUser.getCurrentUser() != null){
            if (!isConnectedToInternet(this)) {
                goOfflineMainActivity()
                return
            }
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
                        if (!isConnectedToInternet(this)) {
                            Log.d(TAG, "not connected")
                            goOfflineMainActivity()
                        }
                        Log.d(TAG, "connected")
                        goMainActivity();
                    }
                }
                AuthorizationResponse.Type.ERROR -> {
                    Toast.makeText(this, response.error, Toast.LENGTH_LONG).show()
                }
                else -> { Log.d(TAG, response.type.toString()) }
            }
        } else if (requestCode == LOGGED_OUT_REQUEST_CODE) {
            etUsername.setText(DEFAULT_USERNAME_TEXT)
            etPassword.setText(DEFAULT_PASSWORD_TEXT)
        }
    }

    private fun logInUser(usernameText: String, passwordText: String) {
        if (!isConnectedToInternet(this)) {
            Toast.makeText(this, TOAST_NOT_CONNECTED_TO_INTERNET, Toast.LENGTH_LONG).show()
        }
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

    private fun goOfflineMainActivity() {
        val intent = Intent(this, OfflineMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Util.INTENT_KEY_SPOTIFY_ACCESS_TOKEN, accessToken)
        startActivityForResult(intent, LOGGED_OUT_REQUEST_CODE)
        finish()
    }

    private fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager
            = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

}