package com.dev.moosic.models

import android.util.Log
import com.dev.moosic.Util
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.parse.ParseClassName
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack
import retrofit.Callback
import retrofit.client.Header
import retrofit.client.Response
import retrofit.mime.TypedString

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
        private const val DUMMY_URL = "url"
        private const val DUMMY_STATUS = 200
        private const val DUMMY_REASON = "reason"
        private val DUMMY_HEADER_LIST : List<Header> = emptyList()
        private const val DUMMY_BODY_STRING = "string"
        private val dummyResponse = Response(
            DUMMY_URL, DUMMY_STATUS,
            DUMMY_REASON, DUMMY_HEADER_LIST,
            TypedString(DUMMY_BODY_STRING)
        )

        const val TAG = "SongFeatures"
        const val KEY_USER_WHO_LOGGED = "loggedBy"
        const val KEY_LOGGED_WEIGHT = "loggedWeight"
        const val KEY_FEATURE_JSON_STRING_DATA = "jsonStringData"

        val FEATURE_KEYS_ARRAY = listOf(
            "acousticness", "danceability", "energy", "instrumentalness",
            "liveness", "speechiness", "valence"
        )

        private const val NULL_PARSE_QUERY = "parse queried objects are null"
        private const val DEFAULT_FEATURE_VALUE = 0.0
        private const val SPOTIFY_QUERY_PARAM_TARGET_PREFIX = "target_%s"
        private const val PARSE_KEY_USERNAME = "username"

        fun fromAudioFeaturesTrack(audioFeatures: AudioFeaturesTrack, weight: Int):
                SongFeatures {
            val features = SongFeatures()
            features.putUserWhoLogged(ParseUser.getCurrentUser())
            features.putLoggedWeight(weight)
            features.putJsonStringData(audioFeatures)
            return features
        }

        fun asyncGetUserPlaylistFeatureMap(user: ParseUser, callback: Callback<Map<String, Double>>){
            val query = ParseQuery.getQuery(SongFeatures::class.java)
            query.include(SongFeatures.KEY_LOGGED_WEIGHT)
            query.include(SongFeatures.KEY_FEATURE_JSON_STRING_DATA)
            query.whereEqualTo(SongFeatures.KEY_USER_WHO_LOGGED, user)
            query.findInBackground { objects, e ->
                if (e != null) {
                    val error = retrofit.RetrofitError.unexpectedError(
                        e.message, e.cause
                    )
                    callback.failure(error)
                } else if (objects == null) {
                    val throwable = Throwable(NULL_PARSE_QUERY)
                    val error = retrofit.RetrofitError.unexpectedError(
                        DUMMY_URL, throwable
                    )
                    callback.failure(error)
                } else {
                    val response = dummyResponse
                    callback.success(featureListToMap(objects), response)
                }
            }
        }

        fun syncGetUserPlaylistFeatureMap(user: ParseUser): Map<String, Double> {
            val query = ParseQuery.getQuery(SongFeatures::class.java)
            query.include(SongFeatures.KEY_LOGGED_WEIGHT)
            query.include(SongFeatures.KEY_FEATURE_JSON_STRING_DATA)
            query.whereEqualTo(SongFeatures.KEY_USER_WHO_LOGGED, user)
            val songFeatures = query.find()
            return featureListToMap(songFeatures)
        }

        fun featureListToMap(songFeatures: List<SongFeatures>): Map<String, Double> {
            val map : MutableMap<String, Double> = mutableMapOf()
            var totalWeight = 0.0
            val gson = Gson()
            for (songFeature in songFeatures) {
                val loggedWeight = songFeature.getLoggedWeight()
                if (loggedWeight != null) {
                    totalWeight += loggedWeight.toDouble()
                }
                val songFeatureJsonStrData = songFeature.getJsonStringData()
                if (songFeatureJsonStrData != null) {
                    val jsonObject = gson.fromJson(songFeatureJsonStrData, JsonObject::class.java)
                    for (feature in FEATURE_KEYS_ARRAY) {
                        val featureValue = jsonObject.get(feature).asNumber
                        val weightedFeatureValue =
                            (if (loggedWeight == null) featureValue.toDouble() else
                                (featureValue.toDouble() * loggedWeight.toDouble()))
                        map.put(feature, map.getOrDefault(feature, DEFAULT_FEATURE_VALUE)
                                + weightedFeatureValue)
                    }
                }
            }
            for (feature in FEATURE_KEYS_ARRAY) {
                map.put(feature, map.getOrDefault(feature,
                    DEFAULT_FEATURE_VALUE) / totalWeight)
            }
            return map.toMap()
        }

        fun featureMapToRecommendationQueryMap(featureMap: Map<String, Double>,
            seedArtists: String, seedGenres: String, seedTracks: String): Map<String, Any> {
            val queryMap : MutableMap<String, Any> = mutableMapOf()
            queryMap.put(Util.SPOTIFY_QUERY_PARAM_SEED_ARTISTS, seedArtists)
            queryMap.put(Util.SPOTIFY_QUERY_PARAM_SEED_GENRES, seedGenres)
            queryMap.put(Util.SPOTIFY_QUERY_PARAM_SEED_TRACKS, seedTracks)
            for (feature in featureMap.keys) {
                queryMap.put(String.format(SPOTIFY_QUERY_PARAM_TARGET_PREFIX, feature),
                    featureMap.getOrDefault(feature, DEFAULT_FEATURE_VALUE) as Number)
            }
            return queryMap.toMap()
        }

        fun syncGetInterestVectorsOfAllUsers(includeSelf : Boolean, friendsToIgnore: List<Contact>)
            : List<Pair<ParseUser, Map<String, Double>>> {
            val query = ParseUser.getQuery()
            val usernamesToIgnore = ArrayList<String>()
            if (!includeSelf) {
                usernamesToIgnore.add(ParseUser.getCurrentUser().username)
            }
            for (friend in friendsToIgnore){
                friend.parseUsername?.let { usernamesToIgnore.add(it) }
            }
            query.whereNotContainedIn(PARSE_KEY_USERNAME, usernamesToIgnore)
            val users = query.find()
            for (user in users) {
                Log.d(TAG, user.username)
            }
            return users.map {
                user -> Pair(user, syncGetUserPlaylistFeatureMap(user))
            }
        }


        fun computeVectorSimilarityScore(vec1: Map<String, Double>,
                                         vec2: Map<String, Double>): Double {
            return dot(vec1, vec2) / (magnitude(vec1) * magnitude(vec2))
        }

        fun dot(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
            var total = 0.0
            for (feature in SongFeatures.FEATURE_KEYS_ARRAY){
                val v1Entry = vec1.getOrDefault(feature, 0.0)
                val v2Entry = vec2.getOrDefault(feature, 0.0)
                if (v1Entry.isNaN() || v2Entry.isNaN()) {
                } else {
                    total += v1Entry * v2Entry
                }
            }
            return total
        }

        fun magnitude(vec: Map<String, Double>): Double {
            var total = 0.0
            for (feature in SongFeatures.FEATURE_KEYS_ARRAY) {
                val entry = vec.getOrDefault(feature, 0.0)
                if (!entry.isNaN()) {
                    total += entry * entry
                }
            }
            return Math.sqrt(total)
        }
    }
}