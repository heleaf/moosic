package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

private const val TAG = "HomeFeedAdapter"

class HomeFeedItemAdapter(context: Context, itemList: ArrayList<Pair<Any, String>>,
                          private val mainActivitySongController: MainActivityControllerInterface,
                          private val playlistController: UserRepoPlaylistControllerInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TAG_TRACK = "track"
        const val TAG_FRIEND_PLAYLIST = "friendPlaylist"
        const val INT_CODE_TRACK = 0
        const val INT_CODE_FRIEND_PLAYLIST = 1
        const val INT_CODE_UNKNOWN = 2
    }
    val itemList: ArrayList<Pair<Any, String>>
    val context: Context

    init {
        this.itemList = itemList
        this.context = context
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList.get(position).second) {
            TAG_FRIEND_PLAYLIST -> INT_CODE_FRIEND_PLAYLIST
            TAG_TRACK -> INT_CODE_TRACK
            else -> INT_CODE_UNKNOWN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            INT_CODE_FRIEND_PLAYLIST -> {
                view = LayoutInflater.from(this.context)
                    .inflate(R.layout.friend_playlist_display_item, parent, false)
                PlaylistViewHolder(view)
            }
            INT_CODE_TRACK -> {
                view = LayoutInflater.from(this.context)
                    .inflate(R.layout.single_track_item, parent, false)
                TrackViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(this.context)
                    .inflate(R.layout.single_track_item, parent, false)
                TrackViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList.get(position)
        when (item.second) {
            TAG_FRIEND_PLAYLIST -> {
                val playlistHolder = holder as PlaylistViewHolder
                playlistHolder.bindPlaylist(item.first as Pair<Contact, ArrayList<Song>>, position)
            }
            TAG_TRACK -> {
                val trackHolder = holder as TrackViewHolder
                trackHolder.bindTrack(item.first as Track)
            }
            else -> {}
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameField : TextView
        private val playlistRv : RecyclerView

        init {
            this.usernameField = itemView.findViewById(R.id.friendPlaylistDisplayParseUsername)
            this.playlistRv = itemView.findViewById(R.id.friendPlaylistSongsRv)
        }

        fun bindPlaylist(pair: Pair<Contact, ArrayList<Song>>, position: Int) {
            val songs = pair.second
            usernameField.visibility = View.GONE

            val playlistSongAdapter = HorizontalPlaylistAdapter(context, songs,
                this@HomeFeedItemAdapter.mainActivitySongController, playlistController)
            playlistRv.adapter = playlistSongAdapter
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false)
            playlistRv.layoutManager = linearLayoutManager
        }
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCover : SimpleDraweeView
        private val songTitle : TextView
        private val artistName : TextView

        private val heartButton : ImageView

        init{
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
        }

        fun bindTrack(track: Track) {
            val trackTitleText = track.name
            songTitle.setText(trackTitleText)

            val artistNameText = track.artists.fold(
                ""
            ) { accumulator, artist ->
                if (artist.name == track.artists.get(0).name) artist.name else
                    accumulator + ", " + artist.name
            }
            artistName.setText(artistNameText)

            try {
                val albumCoverImgUri = track.album.images.get(0).url
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                e.message?.let { Log.e(TAG, it) }
            }

            heartButton.visibility = View.VISIBLE
            val heartIcon = if (playlistController.isInPlaylist(track.id)) R.drawable.ufi_heart_active
            else R.drawable.ufi_heart
            heartButton.setImageResource(heartIcon)

            heartButton.setOnClickListener {
                if (playlistController.isInPlaylist(track.id)) {
                    playlistController.removeFromPlaylist(track.id)
                    heartButton.setImageResource(R.drawable.ufi_heart)
                } else {
                    val gson = Gson()
                    playlistController.addToPlaylist(UserRepositorySong(track.id,
                        gson.toJson(track).toString()), true)
                    heartButton.setImageResource(R.drawable.ufi_heart_active)
                }
            }

            itemView.setOnLongClickListener {
                val gson = Gson()
                playlistController.addToPlaylist(UserRepositorySong(track.id,
                    gson.toJson(track).toString()), true)
                heartButton.setImageResource(R.drawable.ufi_heart_active)
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                this@HomeFeedItemAdapter.mainActivitySongController.playSongOnSpotify(track.uri, track.id)
            }

        }

    }

}