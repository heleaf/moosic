package com.dev.moosic

import android.app.Activity
import android.content.Context
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

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
        builder.setScopes(arrayOf("streaming", "user-top-read", "playlist-modify-public",
            "playlist-read-private", "playlist-modify-private", "user-library-modify",
            "user-library-read", "user-read-private"))
        val request: AuthorizationRequest = builder.build()
        AuthorizationClient.openLoginActivity(currentActivity,
            AUTH_REQUEST_CODE, request)
    }
}