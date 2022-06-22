package com.dev.moosic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.parse.LogInCallback
import com.parse.ParseUser
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.LoginActivity
import org.parceler.Parcels

class LoginActivity : AppCompatActivity() {
//    val CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
    val TAG = "LoginActivity"
    val REDIRECT_URI = "http://localhost:8080"
    private val REQUEST_CODE = 1337

    var mEtUsername : EditText? = null
    var mEtPassword : EditText? = null
    var mLoginButton : Button? = null
    var mSignUpButton : Button? = null

    var accessToken : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mEtUsername = findViewById(R.id.etLoginUsername)
        mEtPassword = findViewById(R.id.etLoginPassword)
        mLoginButton = findViewById(R.id.loginLoginButton)
        mSignUpButton = findViewById(R.id.loginSignUpButton)

        mLoginButton?.setOnClickListener(View.OnClickListener {
            val usernameText = mEtUsername?.text;
            val passwordText = mEtPassword?.text;
            if (usernameText == null || passwordText == null){
                Toast.makeText(this,
                "username or password field is null", Toast.LENGTH_LONG).show()
            }
            else if (usernameText.toString().isEmpty()){
                Toast.makeText(this, "username cannot be empty", Toast.LENGTH_LONG).show()

            } else if (passwordText.toString().isEmpty()){
                Toast.makeText(this, "password cannot be empty", Toast.LENGTH_LONG).show()
            }
            else logInUser(usernameText.toString(), passwordText.toString())
        })

        mSignUpButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
        val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Log.d(TAG, "token: " + response.accessToken)
                }
                AuthorizationResponse.Type.ERROR -> {Log.d(TAG, "error: " + response.error)}
                else -> { Log.d(TAG, response.type.toString()) }
            }
        }
    }

    private fun logInUser(usernameText: String, passwordText: String) {
        ParseUser.logInInBackground(usernameText, passwordText, LogInCallback { user, e ->
            if (e != null){
                Toast.makeText(this, "Issue with login: " + e.message,
                    Toast.LENGTH_LONG).show();
            }
            else goMainActivity()
        })
    }

    private fun goMainActivity() {
        val builder: AuthorizationRequest.Builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )

        builder.setScopes(arrayOf("streaming"))
        val request: AuthorizationRequest = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("accessToken", accessToken)
        startActivity(intent)
    }

}