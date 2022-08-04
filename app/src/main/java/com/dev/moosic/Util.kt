package com.dev.moosic

import android.util.Log
import android.widget.ProgressBar
import androidx.lifecycle.viewModelScope
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.entities.SavedUser
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.dev.moosic.models.UserRepositorySong
import com.dev.moosic.viewmodels.SavedSongsViewModel
import com.google.gson.Gson
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
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

        const val ADD_TO_PLAYLIST_WEIGHT = 2
        const val PLAYED_SONG_WEIGHT = 1

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

        fun prepareRecommendationSeeds(topTracksIdList: List<String>,
                                               topArtistsIdList: List<String>,
                                               userPickedGenresList: ArrayList<String>?)
                : Triple<String, String, String> {
            try {
                if (topTracksIdList.isEmpty() && topArtistsIdList.isEmpty()) {
                    val slicedList = if (userPickedGenresList?.size!! > GENRE_SEEDS_LIMIT)
                        userPickedGenresList.slice(IntRange(0, GENRE_SEEDS_LIMIT - 1))
                    else userPickedGenresList
                    val genreSeed =
                        slicedList.fold(
                            ""
                        ){ accumulatedString, genre ->
                            if (accumulatedString == "") genre else "$accumulatedString,$genre"
                        }
                    return Triple(DUMMY_SEED, DUMMY_SEED,genreSeed)
                }
                else if (userPickedGenresList == null || userPickedGenresList.size == 0) {
                    val trackSeed = topTracksIdList.slice(IntRange(0,2)).fold(
                        ""
                    ) { accumulatedString, trackId ->
                        if (accumulatedString == "") trackId else "$accumulatedString,$trackId"
                    }
                    val artistSeed =
                        topArtistsIdList.slice(IntRange(0,1)).fold(
                            ""
                        ){ accumulatedString, artistId ->
                            if (accumulatedString == "") artistId else "$accumulatedString,$artistId"
                        }
                    return Triple(trackSeed, artistSeed, DUMMY_SEED)
                }
                else {
                    val trackSeed = topTracksIdList.slice(IntRange(0,1)).fold(
                        ""
                    ) { accumulatedString, trackId ->
                        if (accumulatedString == "") trackId else "$accumulatedString,$trackId"
                    }
                    val artistSeed = topArtistsIdList.slice(IntRange(0,1)).fold(
                        ""
                    ){ accumulatedString, artistId ->
                        if (accumulatedString == "") artistId else "$accumulatedString,$artistId"
                    }
                    val genreSeed = userPickedGenresList.get(0)
                    return Triple(trackSeed, artistSeed, genreSeed)
                }
            } catch (e: Exception) {
                return Triple(DUMMY_SEED, DUMMY_SEED, DUMMY_SEED)
            }
        }

        fun extractTopTracksIds(tracks: List<Track>, limit: Int): List<String> {
            val topTrackIds = ArrayList<String>()
            var trackIdx = 0
            while (topTrackIds.size < limit && trackIdx < tracks.size) {
                val track = tracks.get(trackIdx)
                topTrackIds.add(track.id)
                trackIdx += 1
            }
            return topTrackIds
        }
        fun extractTopArtistsIds(tracks: List<Track>, limit: Int): List<String> {
            val topArtistIds = ArrayList<String>()
            var trackIdx = 0
            while (topArtistIds.size < limit && trackIdx < tracks.size) {
                val track = tracks.get(trackIdx)
                val artists = track.artists
                var artistIdx = 0
                while (topArtistIds.size < limit && artistIdx < artists.size) {
                    val artist = artists.get(artistIdx)
                    if (artist.id !in topArtistIds) {
                        topArtistIds.add(artist.id)
                    }
                    artistIdx += 1
                }
                trackIdx += 1
            }
            return topArtistIds
        }

        fun asyncGetRecommendedFriends(includeCurrentUser: Boolean,
                                       contactsToIgnore: List<Contact>,
                                       numberOfUsersRequested: Int,
                                       similarityThreshold: Double,
                                       userPlaylistRepository: UserRepositoryInterface,
                                       callback: Callback<List<Pair<Contact, Double>>>) {
            val userQuery = ParseUser.getQuery()
            val usernamesToIgnore = ArrayList<String>()
            if (!includeCurrentUser) {
                userPlaylistRepository.getUser()?.username?.let { usernamesToIgnore.add(it) }
            }
            for (contact in contactsToIgnore) {
                contact.parseUsername?.let { usernamesToIgnore.add(it) }
            }
            userQuery.whereNotContainedIn(Util.PARSEUSER_KEY_USERNAME, usernamesToIgnore)
            userQuery.findInBackground { objects, e ->
                if (e != null) {
                    val throwable = Throwable(e.message.toString())
                    val error = retrofit.RetrofitError.unexpectedError(
                        Util.DUMMY_URL, throwable
                    )
                    callback.failure(error)
                }
                else if (objects == null) {
                    callback.failure(Util.NULL_SUCCESS_ERROR)
                }
                asyncGetInterestVectorOfUserList(objects!!, 0,
                    ArrayList<Pair<Contact, Map<String, Double>>>(),
                    object: Callback<List<Pair<Contact, Map<String, Double>>>> {
                        override fun success(
                            interestVectors: List<Pair<Contact, Map<String, Double>>>?,
                            response: Response?
                        ) {
                            if (interestVectors == null) {
                                callback.failure(Util.NULL_SUCCESS_ERROR)
                                return
                            }
                            userPlaylistRepository.getUser()?.let {
                                SongFeatures.asyncGetUserPlaylistFeatureMap(
                                    it,
                                    object: Callback<Map<String, Double>> {
                                        override fun success(userInterestVector: Map<String, Double>?, response: Response?) {
                                            if (userInterestVector == null) {
                                                callback.failure(Util.NULL_SUCCESS_ERROR)
                                                return
                                            }
                                            val interestVectorSimilarities : List<Pair<Contact, Double>> = interestVectors.map { userVectorPair ->
                                                Pair(userVectorPair.first,
                                                    SongFeatures.computeVectorSimilarityScore(
                                                        userVectorPair.second,
                                                        userInterestVector))
                                            }
                                            val similaritiesFilteredOutNaNs = interestVectorSimilarities.filter { it -> !it.second.isNaN()
                                            }
                                            val vectorComparator = Comparator { vec1 : Pair<Contact, Double>,
                                                                                vec2 : Pair<Contact, Double> ->
                                                if (vec1.second - vec2.second > 0.0) 1
                                                else if (vec1.second - vec2.second == 0.0) 0
                                                else -1
                                            }
                                            val sortedVectors = similaritiesFilteredOutNaNs.sortedWith(vectorComparator).reversed()
                                            val topVecs = if (sortedVectors.size < numberOfUsersRequested) sortedVectors
                                            else sortedVectors.slice(IntRange(0, numberOfUsersRequested - 1))

                                            val vecsToReturn = topVecs.filter{ pair ->
                                                val similarity = pair.second;
                                                similarity >= similarityThreshold
                                            }

                                            callback.success(vecsToReturn, Util.dummyResponse)
                                        }

                                        override fun failure(error: RetrofitError?) {
                                            callback.failure(error)
                                        }
                                    })
                            }
                        }
                        override fun failure(error: RetrofitError?) {
                            callback.failure(error)
                        }
                    })
            }
        }

        fun asyncGetInterestVectorOfUserList(parseUsers: List<ParseUser>,
                                                     index: Int, interestVectorsAccumulator:
                                                     ArrayList<Pair<Contact, Map<String, Double>>>,
                                                     callback: Callback<List<Pair<Contact, Map<String, Double>>>>) {
            if (index >= parseUsers.size) {
                callback.success(interestVectorsAccumulator, Util.dummyResponse)
            }
            else if (index < 0) {
                callback.failure(Util.INVALID_INDEX_ERROR)
            } else {
                val user = parseUsers.get(index)
                SongFeatures.asyncGetUserPlaylistFeatureMap(user, object: Callback<Map<String,Double>> {
                    override fun success(playlistInterestVector: Map<String, Double>?,
                                         response: Response?) {
                        if (playlistInterestVector != null) {
                            interestVectorsAccumulator.add(Pair(
                                Contact.fromParseUser(user),
                                playlistInterestVector))
                            asyncGetInterestVectorOfUserList(parseUsers,
                                index+1, interestVectorsAccumulator, callback)
                        }
                    }
                    override fun failure(error: RetrofitError?) {
                        callback.failure(error)
                    }
                })
            }
        }

        fun asyncFilterFriendsFromContacts(contactList: List<Contact>,
                                                   userPlaylistRepository: UserRepositoryInterface,
                                                   callback: Callback<Pair<List<Contact>, List<ParseUser>>>) {
            val usersFollowedRelation = userPlaylistRepository.getUser()?.getRelation<ParseUser>(
                Util.PARSEUSER_KEY_USERS_FOLLOWED
            )
            if (usersFollowedRelation != null) {
                asyncGetNonFriendParseUsers(contactList, 0, ArrayList<Contact>(),
                    usersFollowedRelation, object: Callback<List<Contact>> {
                        override fun success(nonFriendParseUserList: List<Contact>?, response: Response?) {
                            if (nonFriendParseUserList != null) {
                                val nonFriendParseUsers = ArrayList<Contact>()
                                nonFriendParseUsers.addAll(nonFriendParseUserList)
                                val friendParseUserQuery = usersFollowedRelation.query
                                friendParseUserQuery.findInBackground { friendParseUsers, e ->
                                    if (e != null) {
                                        val throwable = Throwable(e.message.toString())
                                        val error = retrofit.RetrofitError.unexpectedError(
                                            Util.DUMMY_URL, throwable
                                        )
                                        callback.failure(error)
                                    } else if (friendParseUsers == null) {
                                        callback.failure(Util.NULL_SUCCESS_ERROR)
                                    }

                                    callback.success(Pair(nonFriendParseUsers,
                                        friendParseUsers), Util.dummyResponse)
                                }
                            }
                        }
                        override fun failure(error: RetrofitError?) { }
                    })
            }
        }

        fun asyncExtractFriendPlaylists(friendParseUsers: List<ParseUser>,
                                                index: Int,
                                                accumulator: ArrayList<Pair<Contact, ArrayList<Song>>>,
                                                callback: Callback<ArrayList<Pair<Contact, ArrayList<Song>>>>) {
            if (index >= friendParseUsers.size) {
                callback.success(accumulator, Util.dummyResponse)
            }
            else if (index < 0) {
                callback.failure(Util.INVALID_INDEX_ERROR)
            } else {
                val parseUser = friendParseUsers.get(index)
                val playlistObj = parseUser.getParseObject(Util.PARSEUSER_KEY_PARSE_PLAYLIST)
                val playlistSongsRelation = playlistObj?.getRelation<Song>(Util.PARSEPLAYLIST_KEY_SONGS)
                val query = playlistSongsRelation?.getQuery()
                query?.addDescendingOrder(Util.PARSE_KEY_CREATED_AT)
                query?.findInBackground { objects, e ->
                    if (e != null) callback.failure(
                        retrofit.RetrofitError.unexpectedError(Util.DUMMY_URL, Throwable(e.message)))
                    else if (objects == null) callback.failure(Util.NULL_SUCCESS_ERROR)
                    else {
                        val songs = ArrayList<Song>()
                        songs.addAll(objects)
                        if (songs.size > 0) {
                            accumulator.add(Pair(Contact.fromParseUser(parseUser), songs))
                        }
                        asyncExtractFriendPlaylists(friendParseUsers,
                            index+1, accumulator, callback)
                    }
                }
            }
        }

        fun asyncGetNonFriendParseUsers(contactList: List<Contact>,
                                                index: Int,
                                                accumulator: ArrayList<Contact>,
                                                usersFollowedRelation: ParseRelation<ParseUser>,
                                                callback: Callback<List<Contact>>) {
            if (index >= contactList.size) {
                callback.success(accumulator, Util.dummyResponse)
            }
            else if (index < 0) {
                callback.failure(Util.INVALID_INDEX_ERROR)
            } else {
                val contact = contactList.get(index)
                val usersFollowedQuery = usersFollowedRelation.query
                usersFollowedQuery.whereEqualTo(Util.PARSEUSER_KEY_PHONE_NUMBER,
                    contact.phoneNumber)
                usersFollowedQuery.findInBackground { usersMatched, err ->
                    if (err != null) {
                        val throwable = Throwable(err.message.toString())
                        val error = retrofit.RetrofitError.unexpectedError(
                            Util.DUMMY_URL, throwable
                        )
                        callback.failure(error)
                    }
                    else if (usersMatched == null) {
                        callback.failure(Util.NULL_SUCCESS_ERROR)
                    }
                    else if (usersMatched.size > 0) {
                        asyncGetNonFriendParseUsers(contactList,
                            index+1,
                            accumulator,
                            usersFollowedRelation,
                            callback)
                    } else {
                        val query = ParseQuery.getQuery(ParseUser::class.java)
                        query.whereEqualTo(Util.PARSEUSER_KEY_PHONE_NUMBER,
                            contact.phoneNumber)
                        query.findInBackground { objects, e ->
                            if (e != null) {
                                val throwable = Throwable(e.message.toString())
                                val error = retrofit.RetrofitError.unexpectedError(
                                    Util.DUMMY_URL, throwable
                                )
                                callback.failure(error)
                            }
                            else if (objects == null) {
                                callback.failure(Util.NULL_SUCCESS_ERROR)
                            }
                            else if (objects.size > 0){
                                contact.parseUsername = objects.get(0).username
                                contact.parseUserId = objects.get(0).objectId
                                accumulator.add(contact)
                                asyncGetNonFriendParseUsers(contactList,
                                    index+1,
                                    accumulator,
                                    usersFollowedRelation,
                                    callback)
                            }
                            else {
                                asyncGetNonFriendParseUsers(contactList,
                                    index+1,
                                    accumulator,
                                    usersFollowedRelation,
                                    callback)
                            }
                        }
                    }
                }
            }
        }

        fun getMergedFriendsPlaylist(playlists:  ArrayList<Pair<Contact, ArrayList<Song>>>, songsPerFriend: Int?): ArrayList<Song> {
            val mergedList = ArrayList<Song>()
            for (playlist in playlists){
                val songList = playlist.second
                if (songsPerFriend != null) {
                    var songListIdx = 0
                    while (songListIdx < songList.size && songListIdx < songsPerFriend) {
                        val currSong = songList.get(songListIdx)
                        if (currSong.getSpotifyId() !in mergedList.map{song -> song.getSpotifyId()}){
                            mergedList.add(currSong)
                        }
                        songListIdx += 1
                    }
                } else {
                    mergedList.addAll(songList)
                }
            }
            return mergedList
        }


        fun showProgressBar(progressBar: ProgressBar){
            progressBar.visibility = ProgressBar.VISIBLE
        }

        fun hideProgressBar(progressBar: ProgressBar){
            progressBar.visibility = ProgressBar.GONE
        }


    }
}