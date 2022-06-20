package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.parse.LogInCallback
import com.parse.ParseUser

class LoginActivity : AppCompatActivity() {

    var mEtUsername : EditText? = null
    var mEtPassword : EditText? = null
    var mLoginButton : Button? = null
    var mSignUpButton : Button? = null

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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}