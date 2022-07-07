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
    controller: MainActivity.MainActivityController,
    buttonsToShow: List<String>,
    emptyPlaylistText: TextView?
)
    : RecyclerView.Adapter<SongAdapter.ViewHolder>(){

    var mContext: Context? = null
    var mSongs: ArrayList<Song> = ArrayList()
    val mainActivityController : MainActivity.MainActivityController = controller

    var mShowAddButton = false
    var mShowDeleteButton = false
    var mShowHeartButton = false
    var mShowLogOutButton = false

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
                KEY_LOGOUT_BUTTON -> mShowLogOutButton = true
                else -> {}
            }
        }
        val currentParseUser = ParseUser.getCurrentUser()
        this.mSpotifyUserId = currentParseUser.getString(KEY_USER_SPOTIFYID)
        this.emptyPlaylistText = emptyPlaylistText

        if (this.mSongs.size == 0 || (this.mSongs.size == 1 && mShowLogOutButton)) {
            showEmptyPlaylistText()
        }
    }

    fun showEmptyPlaylistText(){
        this.emptyPlaylistText?.visibility = View.VISIBLE
    }

    override fun getItemViewType(position: Int): Int {
//        return (position == itemCount) ?
        return super.getItemViewType(position)
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
            albumCover = itemView.findViewById(R.id.topTrackImg)
            songTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
            deleteFromPlaylistButton = itemView.findViewById(R.id.deleteFromPlaylistButton)
            logOutRvButton = itemView.findViewById(R.id.logOutRvButton)
        }

        fun bind(song: Song, position: Int) {
            if (mShowLogOutButton && song.getName() == null) {
                // display the logout button
                Log.d("SongAdapter", "displaying logout: " + position + " numItems: " + itemCount)
                listOf(albumCover, songTitle, albumTitle, artistName, heartButton, addToPlaylistButton,
                deleteFromPlaylistButton).map{ item -> item?.visibility = View.GONE }
                logOutRvButton?.visibility = View.VISIBLE
                return
            }

            Log.d("SongAdapter", "song: " + song.getName())
            val jsonDataString = song.getJsonDataString()
            val gson = Gson()
            val track = gson.fromJson(jsonDataString, Track::class.java)
            if (jsonDataString != null) {
                Log.d("SongAdapter", jsonDataString)
                Log.d("SongAdapter", track.toString())

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
//

            try {
                val albumCoverImgUri = song.getImageUri()
                albumCover?.setImageURI(albumCoverImgUri);
                albumCover?.setOnClickListener {
                    song.getSpotifyUri()
                        ?.let { uri -> mainActivityController.playSongOnSpotify(uri, song.getSpotifyId()!!) }
                }
            } catch (e : Exception) {
                Log.e("SongAdapter", "error: " + e.message)
            }

            if (mShowHeartButton) {
                heartButton?.visibility = View.VISIBLE
                heartButton?.setOnClickListener(View.OnClickListener {
//                val isLiked = mainActivityController.tracksAreSaved(listOf(track))
                    mainActivityController.addToSavedTracks(track.id)
                })
            } else {
                heartButton?.visibility = View.GONE
            }

            if (mShowAddButton) {
                addToPlaylistButton?.visibility = View.VISIBLE
                addToPlaylistButton?.setOnClickListener(View.OnClickListener {
                    mainActivityController.addToParsePlaylist(track, mShowLogOutButton)
//                    this@SongAdapter.notifyItemInserted(mSongs.size - 1)
//                    this@SongAdapter.notifyItemRangeChanged(mSongs.size - 2, 2)
                })
            } else {
                addToPlaylistButton?.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    mainActivityController.removeFromParsePlaylist(track, position)
//                    mSongs.removeAt(position)
//                    this@SongAdapter.notifyDataSetChanged()
                    this@SongAdapter.notifyItemRemoved(position)
//                    this@SongAdapter.notifyItemRangeRemoved(position, 1)
                    this@SongAdapter.notifyItemRangeChanged(position, mSongs.size)
//                    this@SongAdapter.notifyItemRangeChanged()

                    // this@SongAdapter.notifyItemRangeRemoved(position + 1, 1)

                    if (mSongs.size == 0 || (mSongs.size == 1 && mShowLogOutButton)) {
                        showEmptyPlaylistText()
                    }
                })
            } else {
                deleteButton.visibility = View.GONE
            }

        }
    }

}