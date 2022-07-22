package com.dev.moosic.controllers

import android.app.Activity
import com.dev.moosic.controllers.AuthorizationController
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

private const val AUTH_STREAMING = "streaming"
private const val AUTH_USER_TOP_READ = "user-top-read"
private val AUTH_SCOPE_ARRAY = arrayOf(AUTH_STREAMING, AUTH_USER_TOP_READ)

class SpotifyAuthController(activity: Activity) : AuthorizationController {
    var currentActivity : Activity
    init {
        currentActivity = activity
    }
    override fun authorizeUser() {
        val builder: AuthorizationRequest.Builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
        builder.setScopes(AUTH_SCOPE_ARRAY)
        val request: AuthorizationRequest = builder.build()
        AuthorizationClient.openLoginActivity(currentActivity,
            AUTH_REQUEST_CODE, request)
    }
}