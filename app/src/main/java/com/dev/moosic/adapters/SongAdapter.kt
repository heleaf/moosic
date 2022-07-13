package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

private const val KEY_ADD_BUTTON = "add"
private const val KEY_DELETE_BUTTON = "delete"
private const val KEY_HEART_BUTTON = "heart"

private const val KEY_LOGOUT_BUTTON = "logOut"

private const val KEY_USER_SPOTIFYID = "userId"

class SongAdapter(
    context: Context,
    songs: ArrayList<Song>,
    controller: MainActivity.MainActivitySongController,
    buttonsToShow: List<String>,
    emptyPlaylistText: TextView?
)
    : RecyclerView.Adapter<SongAdapter.ViewHolder>(){

    var mContext: Context? = null
    var mSongs: ArrayList<Song> = ArrayList()
    val mainActivitySongController : MainActivity.MainActivitySongController = controller

    var mShowAddButton = false
    var mShowDeleteButton = false
    var mShowHeartButton = false

    var mSpotifyUserId : String? = null

    var emptyPlaylistText: TextView? = null

    init {
        this.mContext = context
        this.mSongs = songs
        for (str in buttonsToShow){
            when (str) {
                KEY_ADD_BUTTON -> mShowAddButton = true
                KEY_DELETE_BUTTON -> mShowDeleteButton = true
                KEY_HEART_BUTTON -> mShowHeartButton = true
                else -> {}
            }
        }
        val currentParseUser = ParseUser.getCurrentUser()
        this.mSpotifyUserId = currentParseUser.getString(KEY_USER_SPOTIFYID)
        this.emptyPlaylistText = emptyPlaylistText

        if (this.mSongs.size == 0) {
            showEmptyPlaylistText()
        }
    }

    fun showEmptyPlaylistText(){
        this.emptyPlaylistText?.visibility = View.VISIBLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(
            R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = this.mSongs.get(position)
        holder.bind(song, position)
    }

    override fun getItemCount(): Int {
        Log.d("SongAdapter", this.mSongs.size.toString())
        return this.mSongs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView? = null
        var songTitle : TextView? = null
        var albumTitle : TextView? = null
        var artistName : TextView? = null

        var heartButton : ImageView? = null
        var addToPlaylistButton : ImageView? = null
        var deleteFromPlaylistButton : ImageView? = null

        var logOutRvButton: Button? = null

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
            deleteFromPlaylistButton = itemView.findViewById(R.id.deleteFromPlaylistButton)
            logOutRvButton = itemView.findViewById(R.id.logOutRvButton)
        }

        fun bind(song: Song, position: Int) {
            val jsonDataString = song.getJsonDataString()
            val gson = Gson()
            val track = gson.fromJson(jsonDataString, Track::class.java)
            if (jsonDataString != null) {
                albumTitle?.setText(track.album.name)
                val artistNameText = track.artists.fold(
                    ""
                ) { accumulator, artist ->
                    if (artist.name == track.artists.get(0).name) artist.name else
                        accumulator + ", " + artist.name
                }
                artistName?.setText(artistNameText)
            }

            songTitle?.setText(song.getName())

            itemView.setOnClickListener {
                val id = song.getSpotifyId()
                if (id != null){
                    song.getSpotifyUri()
                        ?.let { uri ->
                            mainActivitySongController.playSongOnSpotify(
                                uri,
                                id
                            )
                        }
                }
            }

            try {
                val albumCoverImgUri = song.getImageUri()
                albumCover?.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                Log.e("SongAdapter", "error: " + e.message)
            }

            if (mShowHeartButton) {
                heartButton?.visibility = View.VISIBLE
                heartButton?.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.addToSavedTracks(track.id)
                })
            } else {
                heartButton?.visibility = View.GONE
            }

            if (mShowAddButton) {
                addToPlaylistButton?.visibility = View.VISIBLE
                addToPlaylistButton?.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.addToParsePlaylist(track)
                })
            } else {
                addToPlaylistButton?.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.removeFromParsePlaylist(track, position)
                    this@SongAdapter.notifyItemRemoved(position)
                    this@SongAdapter.notifyItemRangeChanged(position, mSongs.size)
                    if (mSongs.size == 0) {
                        showEmptyPlaylistText()
                    }
                })
            } else {
                deleteButton.visibility = View.GONE
            }

        }
    }

}