package com.dev.moosic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.dev.moosic.models.Playlist
import com.google.gson.Gson
import com.parse.ParseObject
import com.parse.ParseUser
import org.parceler.Parcels

class SignUpActivity : AppCompatActivity() {
    val TAG = "SignUpActivity"
    val KEY_PHONE_NUMBER = "phoneNumber"
    val REQUEST_CODE_GET_INTERESTS = 1999

    val KEY_USER_PICKED_GENRES = "userPickedGenres"

    var mUsername : EditText? = null
    var mPassword : EditText? = null
    var mEmail : EditText? = null
    var mPhoneNumber : EditText? = null
    var mSignUpButton : Button? = null

    var accessToken: String? = null

    var user : ParseUser = ParseUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mUsername = findViewById(R.id.etSignUpUsername)
        mPassword = findViewById(R.id.etSignUpPassword)
        mEmail = findViewById(R.id.etSignUpEmail)
        mPhoneNumber = findViewById(R.id.etSignUpPhone)
        mSignUpButton = findViewById(R.id.signUpSignUpButton)

        mSignUpButton?.setOnClickListener(View.OnClickListener {
            val usernameText = mUsername?.text
            val passwordText = mPassword?.text
            val emailText = mEmail?.text
            val phoneText = mPhoneNumber?.text

            if (usernameText == null || passwordText == null ||
                    emailText == null || phoneText == null){
                Log.d(TAG, "EditTexts or their fields are null")
            } else signUp(usernameText.toString(), passwordText.toString(),
            emailText.toString(), phoneText.toString())

        })

        accessToken = intent.getStringExtra("accessToken")
        Log.d(TAG, "accesstoken: " + accessToken)

    }

    private fun signUp(username: String, password: String, email: String, phone: String) {
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
                val playlist = Playlist()
                playlist.saveInBackground { e ->
                    if (e != null) Log.d(TAG, "error saving playlist: " + e.message)
                    else {
                        user.put("parsePlaylist", playlist)
                        user.saveInBackground()

                        Log.d(TAG, "wtf?")

                        // get user interests
                        val intent = Intent(this, GetInterestsActivity::class.java)
                        intent.putExtra("user", Parcels.wrap(user))
                        intent.putExtra("accessToken", accessToken)
                        startActivityForResult(intent, REQUEST_CODE_GET_INTERESTS)

                        mUsername?.setText("")
                        mPassword?.setText("")
                        mEmail?.setText("")
                        mPhoneNumber?.setText("")
                    }
                }
            }
        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_GET_INTERESTS) {
            if (resultCode == RESULT_OK){
                val genres : ArrayList<String>
                    = Parcels.unwrap(data?.getParcelableExtra("userPickedGenres"))
                // add these interests to the parse database
                val gson = Gson()
                val pickedGenresJsonString = gson.toJson(genres)
                user.put(KEY_USER_PICKED_GENRES, pickedGenresJsonString)
                user.saveInBackground {
                    if (it != null) {
                        Log.d(TAG, "error saving user's genres: " + it.message)
                        return@saveInBackground
                    }
                    finish() // go back
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}