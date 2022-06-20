package com.dev.moosic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.parse.ParseUser

class SignUpActivity : AppCompatActivity() {
    val TAG = "SignUpActivity"
    val KEY_PHONE_NUMBER = "phoneNumber"

    var mUsername : EditText? = null
    var mPassword : EditText? = null
    var mEmail : EditText? = null
    var mPhoneNumber : EditText? = null
    var mSignUpButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mUsername = findViewById(R.id.etSignUpUsername)
        mPassword = findViewById(R.id.etSignUpPassword)
        mEmail = findViewById(R.id.etSignUpEmail)
        mPhoneNumber = findViewById(R.id.etSignUpPhone)
        mSignUpButton = findViewById(R.id.signUpSignUpButton)

        mSignUpButton?.setOnClickListener(View.OnClickListener {
            var usernameText = mUsername?.text
            var passwordText = mPassword?.text
            var emailText = mEmail?.text
            var phoneText = mPhoneNumber?.text

            if (usernameText == null || passwordText == null ||
                    emailText == null || phoneText == null){
                Log.d(TAG, "EditTexts or their fields are null")
            } else signUp(usernameText.toString(), passwordText.toString(),
            emailText.toString(), phoneText.toString())

        })

    }

    private fun signUp(username: String, password: String, email: String, phone: String) {
        val user = ParseUser()
        user.setUsername(username)
        user.setPassword(password)
        user.setEmail(email)

        // TODO: this method is unreliable, need better way of checking phone # validity
        if (!PhoneNumberUtils.isGlobalPhoneNumber(phone)){
            Toast.makeText(this, phone + " is not a valid phone number",
                Toast.LENGTH_LONG).show()
            return
        }

        user.put(KEY_PHONE_NUMBER, phone)

        user.signUpInBackground {
            if (it != null){
                Toast.makeText(this,
                "Error signing up: " + it.message,
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,
                "Successfully signed up", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }
}