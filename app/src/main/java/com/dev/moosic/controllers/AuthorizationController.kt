package com.dev.moosic.controllers

import com.dev.moosic.Util

interface AuthorizationController {
    val CLIENT_ID: String
        get() = Util.SPOTIFY_APK_CLIENT_ID
    val REDIRECT_URI: String
        get() = Util.SPOTIFY_APK_REDIRECT_URI
    val AUTH_REQUEST_CODE: Int
        get() = Util.REQUEST_CODE_USER_AUTH
    fun authorizeUser()
}