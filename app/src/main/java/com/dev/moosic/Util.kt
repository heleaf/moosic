package com.dev.moosic

import androidx.lifecycle.viewModelScope
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.entities.SavedUser
import com.dev.moosic.models.UserRepositorySong
import com.dev.moosic.viewmodels.SavedSongsViewModel
import com.google.gson.Gson
import com.parse.ParseUser
import kotlinx.coroutines.launch
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Header
import retrofit.client.Response
import retrofit.mime.TypedString
import java.util.function.Function

class Util {
    companion object {
        const val PARSEUSER_KEY_USERS_FOLLOWED = "usersFollowed"
        const val PARSEUSER_KEY_PHONE_NUMBER = "phoneNumber"
        const val PARSEUSER_KEY_USERNAME = "username"
        const val PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME = "userId"
        const val PARSEUSER_KEY_PARSE_PLAYLIST = "parsePlaylist"
        const val PARSEUSER_KEY_FAVORITE_GENRES = "userPickedGenres"

        const val PARSEPLAYLIST_KEY_SONGS = "playlistSongs"
        const val PARSESONG_KEY_SPOTIFY_ID = "spotifyId"
        const val PARSE_KEY_CREATED_AT = "createdAt"

        const val SPOTIFY_APK_CLIENT_ID = "7b7fed9bf37945818d20992b055ac63b"
        const val SPOTIFY_APK_REDIRECT_URI = "http://localhost:8080"

        const val SPOTIFY_QUERY_PARAM_SEED_TRACKS = "seed_tracks"
        const val SPOTIFY_QUERY_PARAM_SEED_ARTISTS = "seed_artists"
        const val SPOTIFY_QUERY_PARAM_SEED_GENRES = "seed_genres"
        const val SPOTIFY_QUERY_PARAM_OFFSET = "offset"
        const val SPOTIFY_QUERY_PARAM_LIMIT = "limit"

        const val SPOTIFY_URI_PREFIX = "spotify:track:"

        fun getSpotifyIdFromUri(uri: String) : String {
            if (uri.length < SPOTIFY_URI_PREFIX.length) { return "" }
            return uri.slice(IntRange(SPOTIFY_URI_PREFIX.length, uri.length - 1))
        }

        fun getSpotifyUriFromSpotifyId(spotifyId: String) : String {
            return "${SPOTIFY_URI_PREFIX}$spotifyId"
        }

        const val DUMMY_URL = "url"
        private const val DUMMY_STATUS = 200
        private const val DUMMY_REASON = "reason"
        private val DUMMY_HEADER_LIST : List<Header> = emptyList()
        private const val DUMMY_BODY_STRING = "string"
        val dummyResponse = Response(
            DUMMY_URL, DUMMY_STATUS,
            DUMMY_REASON, DUMMY_HEADER_LIST,
            TypedString(DUMMY_BODY_STRING)
        )

        const val THROWABLE_NULL_SUCCESS_MESSAGE = "Objects on success are null"
        val NULL_SUCCESS_ERROR: RetrofitError = retrofit.RetrofitError.unexpectedError(
            DUMMY_URL, Throwable(THROWABLE_NULL_SUCCESS_MESSAGE))

        private const val THROWABLE_INVALID_INDEX_MESSAGE = "Invalid index"
        val INVALID_INDEX_ERROR: RetrofitError = retrofit.RetrofitError.unexpectedError(
            DUMMY_URL, Throwable(THROWABLE_INVALID_INDEX_MESSAGE))

        const val INTENT_KEY_SPOTIFY_ACCESS_TOKEN = "accessToken"
        const val INTENT_KEY_USER_PICKED_GENRES = "userPickedGenres"
        const val INTENT_KEY_USERNAME_TEXT = "usernameText"
        const val INTENT_KEY_PASSWORD_TEXT = "passwordText"
        const val INTENT_KEY_NEW_USER = "user"

        const val REQUEST_CODE_USER_AUTH = 1337
        const val REQUEST_CODE_GET_INTERESTS = 1999
        const val REQUEST_CODE_SETTINGS = 2000
        const val RESULT_CODE_LOG_OUT = 2001
        const val RESULT_CODE_EXIT_SETTINGS = 2002

        const val TOAST_FAILED_LOGOUT = "Failed to log out"

        fun saveTopTenSongs(viewModel: SavedSongsViewModel, db: LocalDatabase) {
            viewModel.viewModelScope.launch {
                val gson = Gson()
                val savedSongsObject = gson.toJson(viewModel.songs)
                val currentUsername = ParseUser.getCurrentUser().username
                val currentUserId = ParseUser.getCurrentUser().objectId
                val user = db.userDao().getUser(currentUsername)
                if (user == null) {
                    db.userDao().insertUserInfo(SavedUser(currentUsername, currentUserId, savedSongsObject))
                } else {
                    db.userDao().updateUserInfo(SavedUser(currentUsername, currentUserId, savedSongsObject))
                }
            }
        }

        fun extractTopTenSongs(viewModel: SavedSongsViewModel, db: LocalDatabase, successFunction: Function<Unit, Unit>) {
            viewModel.viewModelScope.launch {
                val user = db.userDao().getUser(ParseUser.getCurrentUser().username)
                if (user != null) {
                    val gson = Gson()
                    val songStr = user.savedSongs
                    val songList : Array<UserRepositorySong> = gson.fromJson(songStr,
                        Array<UserRepositorySong>::class.java)
                    viewModel.songs.addAll(songList)
                    successFunction.apply(Unit)
                }
            }
        }

    }
}