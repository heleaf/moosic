package com.dev.moosic

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.adapters.*
import com.dev.moosic.controllers.SongController
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Header
import retrofit.client.Response
import retrofit.mime.TypedString
import java.lang.Exception

class Util {
    companion object {
        const val FLAG_ADD_BUTTON = "add"
        const val FLAG_DELETE_BUTTON = "delete"
        const val FLAG_HEART_BUTTON = "heart"

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

        fun bind_single_track_item(track: Track, position: Int,
                                   itemView: View, trackTitle: TextView, artistName: TextView, albumCover: SimpleDraweeView,
                                   heartButton: ImageView, controller: SongController, adapter: SongAdapter) {
            itemView.setOnClickListener {
                controller.playSongOnSpotify(track.uri, track.id)
            }

            val trackTitleText = track.name
            trackTitle.setText(trackTitleText)

            val artistNameText = track.artists.fold(
                ""
            ) { accumulator, artist ->
                if (artist.name == track.artists.get(0).name) artist.name else
                    "$accumulator, ${artist.name}"
            }
            artistName.setText(artistNameText)

            try {
                val albumCoverImgUri = track.album.images.get(0).url
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) { }

            heartButton.visibility = View.VISIBLE

            var isInPlaylist = false
            controller.isInPlaylist(track, object: Callback<Boolean> {
                override fun success(t: Boolean?, response: Response?) {
                    if (t==null) {heartButton.visibility = View.GONE; return }
                    val heartIcon = if (t == true) R.drawable.ufi_heart_active else R.drawable.ufi_heart
                    heartButton.setImageResource(heartIcon)
                    isInPlaylist = t
                }
                override fun failure(error: RetrofitError?) {
                    heartButton.visibility = View.GONE
                }
            })

//            heartButton.setOnClickListener(View.OnClickListener {
//                if (isInPlaylist) {
//                    controller.removeFromPlaylist(track, object: Callback<Unit> {
//                        override fun success(t: Unit?, response: Response?) {
//                            isInPlaylist = !isInPlaylist
//                            removeFromPlaylistSuccess(heartButton, adapter)
//                        }
//                        override fun failure(error: RetrofitError?) {}
//                    })
//                } else {
//                    controller.addToPlaylist(track, object: Callback<Unit> {
//                        override fun success(t: Unit?, response: Response?) {
//                            isInPlaylist = !isInPlaylist
//                            heartButton.setImageResource(R.drawable.ufi_heart_active)
//                        }
//                        override fun failure(error: RetrofitError?) {}
//                    })
//                }
//            })
        }

        fun bind_single_track_item(track: Track, position: Int, viewholder: RecyclerView.ViewHolder,
                                   trackLayout: View, trackTitle: TextView, artistName: TextView, albumCover: SimpleDraweeView,
                                   heartButton: ImageView, controller: SongController, adapter: TrackAdapter) {
        }


//        fun bind_single_track_item(track: Track, position: Int, viewholder: RecyclerView.ViewHolder,
//                                   trackLayout: View, trackTitle: TextView, artistName: TextView, albumCover: SimpleDraweeView,
//                                   heartButton: ImageView, controller: SongController, adapter: HomeFeedItemAdapter) {
//        }


    }
}