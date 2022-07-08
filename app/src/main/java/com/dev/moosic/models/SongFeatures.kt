package com.dev.moosic.models

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.parse.ParseClassName
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit.mime.TypedInput
import retrofit.mime.TypedString
import java.io.IOException

@ParseClassName("SongFeatures")
class SongFeatures() : ParseObject() {
    fun getUserWhoLogged(): ParseUser {
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
        val TAG = "SongFeatures"
        val KEY_USER_WHO_LOGGED = "loggedBy"
        val KEY_LOGGED_WEIGHT = "loggedWeight"
        val KEY_FEATURE_JSON_STRING_DATA = "jsonStringData"

        val FEATURE_KEYS_ARRAY = listOf(
            "acousticness", "danceability", "energy", "instrumentalness",
            "liveness", "speechiness", "valence"
        )

        fun fromAudioFeaturesTrack(audioFeatures: AudioFeaturesTrack, weight: Int):
                SongFeatures {
            val features = SongFeatures()
            features.putUserWhoLogged(ParseUser.getCurrentUser())
            features.putLoggedWeight(weight)
            features.putJsonStringData(audioFeatures)
            return features
        }

        fun asyncGetUserPlaylistFeatureMap(callback: Callback<Map<String, Double>>){
            val query = ParseQuery.getQuery(SongFeatures::class.java)
            query.include(SongFeatures.KEY_LOGGED_WEIGHT)
            query.include(SongFeatures.KEY_FEATURE_JSON_STRING_DATA)
            query.whereEqualTo(SongFeatures.KEY_USER_WHO_LOGGED, ParseUser.getCurrentUser())
            val songFeatures = query.findInBackground { objects, e ->
                if (e != null) {
                    // TODO: shouldn't pass in the message for the url parameter
                    val error = retrofit.RetrofitError.unexpectedError(
                        e.message, e.cause
                    )
                    callback.failure(error)
                } else if (objects == null) {
                    // TODO: shouldn't pass in the message for the url parameter
                    val throwable = Throwable("objects are null")
                    val error = retrofit.RetrofitError.unexpectedError(
                        "no url", throwable
                    )
                    callback.failure(error)
                } else {
                    // TODO: create actual valid response
                    val response =  Response(
                        "url", 200,
                        "reason", emptyList(),
                        TypedString("string")
                    )
                    callback.success(featureListToMap(objects), response)
                }
            }
        }

        fun syncGetUserPlaylistFeatureMap(/*callback: Callback<Map<String,Number>>*/): Map<String, Double> {
            val query = ParseQuery.getQuery(SongFeatures::class.java)
            query.include(SongFeatures.KEY_LOGGED_WEIGHT)
            query.include(SongFeatures.KEY_FEATURE_JSON_STRING_DATA)
            query.whereEqualTo(SongFeatures.KEY_USER_WHO_LOGGED, ParseUser.getCurrentUser())
            val songFeatures = query.find()
            return featureListToMap(songFeatures)
        }

        fun featureListToMap(songFeatures: List<SongFeatures>): Map<String, Double> {
            val map : MutableMap<String, Double> = mutableMapOf()
            var totalWeight = 0.0
            val gson = Gson()
            for (songFeature in songFeatures) {
                val loggedWeight = songFeature.getLoggedWeight()
//                Log.d(TAG, "logged weight for song: " + loggedWeight?.toDouble().toString())
                if (loggedWeight != null) {
                    totalWeight += loggedWeight.toDouble()
                }
                val songFeatureJsonStrData = songFeature.getJsonStringData()
                if (songFeatureJsonStrData != null) {
//                    Log.d(TAG, "not null song feature json str data")
                    val jsonObject = gson.fromJson(songFeatureJsonStrData, JsonObject::class.java)
                    for (feature in FEATURE_KEYS_ARRAY) {
                        val featureValue = jsonObject.get(feature).asNumber
//                        Log.d(TAG, "feature: " + feature + " value: " + featureValue.toString())
                        val weightedFeatureValue =
                            (if (loggedWeight == null) featureValue.toDouble() else
                                (featureValue.toDouble() * loggedWeight.toDouble()))
//                        Log.d(TAG, "feature: " + feature + " weighted value: " + weightedFeatureValue)
                        map.put(feature, map.getOrDefault(feature, 0.0) + weightedFeatureValue)
                    }
                }
            }
//            Log.d(TAG, "total weight: " + totalWeight)
            // normalize by the total weight
            for (feature in FEATURE_KEYS_ARRAY) {
                map.put(feature, map.getOrDefault(feature, 0.0) / totalWeight)
            }
            return map.toMap()
        }
    }
}