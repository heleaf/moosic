package com.dev.moosic.controllers

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.dev.moosic.Util
import com.parse.ParseUser

class SettingsController(private val activity: Activity) : SettingsControllerInterface {
    override fun logOut(loadingBar: ProgressBar) {
        loadingBar.visibility = View.VISIBLE
        ParseUser.logOutInBackground {
            if (it != null) {
                Toast.makeText(activity,
                    "${Util.TOAST_FAILED_LOGOUT}: ${it.message}",
                    Toast.LENGTH_LONG).show()
                loadingBar.visibility = View.INVISIBLE
            }
            else {
                activity.setResult(Util.RESULT_CODE_LOG_OUT)
                loadingBar.visibility = View.INVISIBLE
                activity.finish()
            }
        }
    }
}