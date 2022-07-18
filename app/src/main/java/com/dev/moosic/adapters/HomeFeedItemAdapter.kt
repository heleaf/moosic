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
import com.dev.moosic.controllers.SongController
import com.dev.moosic.models.Contact
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception

private const val TAG = "HomeFeedAdapter"

class HomeFeedItemAdapter(context: Context, itemList: ArrayList<Pair<Any, String>>,
    controller: SongController) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TAG_TRACK = "track"
        const val TAG_FRIEND_PLAYLIST = "friendPlaylist"
        const val INT_CODE_TRACK = 0
        const val INT_CODE_FRIEND_PLAYLIST = 1
        const val INT_CODE_UNKNOWN = 2
    }
    var itemList: ArrayList<Pair<Any, String>>
    var context: Context
    var controller: SongController

    init {
        this.itemList = itemList
        this.context = context
        this.controller = controller
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
            val contact = pair.first
            val songs = pair.second
            if (contact.parseUsername != null) {
                usernameField.setText(contact.parseUsername)
            }
            val playlistSongAdapter = HorizontalPlaylistAdapter(context, songs, controller)
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

            fun removeFromPlaylistSuccess() {
                heartButton.setImageResource(R.drawable.ufi_heart)
                isInPlaylist = !isInPlaylist
            }

            fun addToPlaylistSuccess() {
                heartButton.setImageResource(R.drawable.ufi_heart_active)
                isInPlaylist = !isInPlaylist
            }

            heartButton.setOnClickListener(View.OnClickListener {
                if (isInPlaylist) {
                    controller.removeFromPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            removeFromPlaylistSuccess()
                        }
                        override fun failure(error: RetrofitError?) {}
                    })
                } else {
                    controller.addToPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            addToPlaylistSuccess()
                        }
                        override fun failure(error: RetrofitError?) {}

                    })
                }

            })

            itemView.setOnLongClickListener {
                controller.addToPlaylist(track, object: Callback<Unit> {
                    override fun success(t: Unit?, response: Response?) {
                        addToPlaylistSuccess()
                    }
                    override fun failure(error: RetrofitError?) {}
                })
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                controller.playSongOnSpotify(track.uri, track.id)
            }

        }
    }

}