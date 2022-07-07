package com.dev.moosic.models

import com.google.gson.Gson
import com.parse.ParseClassName
import com.parse.ParseObject
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack

@ParseClassName("SongFeatures")
class SongFeatures() : ParseObject() {
    val KEY_USER_WHO_LOGGED = "loggedBy"
    val KEY_LOGGED_WEIGHT = "loggedWeight"
    val KEY_FEATURE_JSON_STRING_DATA = "jsonStringData"

    // idk if this will work..
    fun getUserWhoLogged(): ParseUser {
        // getParseObject?
        return getParseObject(KEY_USER_WHO_LOGGED) as ParseUser
    }
    fun putUserWhoLogged(user: ParseUser){
        put(KEY_USER_WHO_LOGGED, user)
    }

    fun getLoggedWeight(): Number? {
        return getNumber(KEY_LOGGED_WEIGHT)
    }

    fun putLoggedWeight(weight: Number){
        put(KEY_LOGGED_WEIGHT, weight)
    }

    fun getJsonStringData(): String? {
        return getString(KEY_FEATURE_JSON_STRING_DATA)
    }

    fun putJsonStringData(features: AudioFeaturesTrack) {
        val gson = Gson()
        val strData = gson.toJson(features).toString()
        put(KEY_FEATURE_JSON_STRING_DATA, strData)
    }

    companion object Factory {
        fun fromAudioFeaturesTrack(audioFeatures: AudioFeaturesTrack, weight: Int):
                SongFeatures {
            val features = SongFeatures()
            features.putUserWhoLogged(ParseUser.getCurrentUser())
            features.putLoggedWeight(weight)
            features.putJsonStringData(audioFeatures)
            return features
        }
    }
}