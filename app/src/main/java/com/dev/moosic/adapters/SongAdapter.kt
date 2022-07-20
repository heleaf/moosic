package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.Util
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception

private const val TAG = "SongAdapter"
private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class SongAdapter(
    context: Context,
    songs: ArrayList<Song>,
    controller: MainActivity.MainActivitySongController,
    buttonsToShow: List<String>,
    emptyPlaylistText: TextView?
)
    : RecyclerView.Adapter<SongAdapter.ViewHolder>(){

    var context: Context
    var songs: ArrayList<Song> = ArrayList()
    val mainActivitySongController : MainActivity.MainActivitySongController = controller
    var showAddButton = false
    var showDeleteButton = false
    var showHeartButton = false
    var spotifyUserId : String? = null
    var emptyPlaylistText: TextView?

    init {
        this.context = context
        this.songs = songs
        for (str in buttonsToShow){
            when (str) {
                Util.FLAG_ADD_BUTTON -> showAddButton = true
                Util.FLAG_DELETE_BUTTON -> showDeleteButton = true
                Util.FLAG_HEART_BUTTON -> showHeartButton = true
                else -> {}
            }
        }
        val currentParseUser = ParseUser.getCurrentUser()
        this.spotifyUserId = currentParseUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME)
        this.emptyPlaylistText = emptyPlaylistText
        if (this.songs.isEmpty()) {
            showEmptyPlaylistText()
        }
    }

    fun showEmptyPlaylistText(){
        this.emptyPlaylistText?.visibility = View.VISIBLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(
            R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = this.songs.get(position)
        holder.bind(song, position)
    }

    override fun getItemCount(): Int {
        return this.songs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView
        var songTitle : TextView
        var artistName : TextView
        var heartButton : ImageView

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
        }

        fun bind(song: Song, position: Int) {
            val jsonDataString = song.getJsonDataString()
            val gson = Gson()
            val track = gson.fromJson(jsonDataString, Track::class.java)
            if (jsonDataString != null) {
                val artistNameText = track.artists.fold(
                    EMPTY_STR
                ) { accumulator, artist ->
                    if (artist.name == track.artists.get(0).name) artist.name else
                        accumulator + ARTIST_STR_SEPARATOR + artist.name
                }
                artistName.setText(artistNameText)
            }

            songTitle.setText(song.getName())

            itemView.setOnClickListener {
                val id = song.getSpotifyId()
                if (id != null){
                    song.getSpotifyUri()
                        ?.let { uri -> mainActivitySongController.playSongOnSpotify(uri, id) }
                }
            }

            try {
                val albumCoverImgUri = song.getImageUri()
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                e.message?.let { Log.e(TAG, it) }
            }

            heartButton.visibility = View.VISIBLE
            var isInPlaylist = false
            mainActivitySongController.isInPlaylist(track, object: Callback<Boolean> {
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

            heartButton.setOnClickListener(View.OnClickListener {
                if (isInPlaylist) {
                    mainActivitySongController.removeFromPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            heartButton.setImageResource(R.drawable.ufi_heart)
                            isInPlaylist = !isInPlaylist
                            this@SongAdapter.notifyItemRemoved(position)
                            this@SongAdapter.notifyItemRangeChanged(position, songs.size)
                            if (songs.isEmpty()) {
                                showEmptyPlaylistText()
                            }
                        }
                        override fun failure(error: RetrofitError?) {}
                    })
                } else {
                    mainActivitySongController.addToPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            isInPlaylist = !isInPlaylist
                            heartButton.setImageResource(R.drawable.ufi_heart_active)
                        }

                        override fun failure(error: RetrofitError?) {}

                    })
                }

            })

        }
    }

}