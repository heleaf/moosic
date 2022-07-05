package com.dev.moosic

interface AuthorizationController {
    val CLIENT_ID: String
        get() = "7b7fed9bf37945818d20992b055ac63b"
    val REDIRECT_URI: String
        get() = "http://localhost:8080"
    val AUTH_REQUEST_CODE: Int
        get() = 1337
    fun authorizeUser()
}