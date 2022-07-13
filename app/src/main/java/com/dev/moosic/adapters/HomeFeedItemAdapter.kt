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
import java.lang.Exception

class HomeFeedItemAdapter(context: Context, itemList: ArrayList<Pair<Any, String>>,
    controller: SongController) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        val TAG_TRACK = "track"
        val INT_CODE_TRACK = 0

        val TAG_FRIEND_PLAYLIST = "friendPlaylist"
        val INT_CODE_FRIEND_PLAYLIST = 1

        val INT_CODE_UNKNOWN = 2
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
                view = LayoutInflater.from(this.context).inflate(R.layout.friend_playlist_display_item, parent, false)
                PlaylistViewHolder(view)
            }
            INT_CODE_TRACK -> {
                view = LayoutInflater.from(this.context).inflate(R.layout.single_track_item, parent, false)
                TrackViewHolder(view)
            }
            // TODO: return something else here
            else -> {
                view = LayoutInflater.from(this.context).inflate(R.layout.single_track_item, parent, false)
                TrackViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList.get(position)
        when (item.second) {
            TAG_FRIEND_PLAYLIST -> {
                Log.e("HomeFeedMixed adapter", item.toString())
                Log.e("HomeFeedMixed adapter", item.first.toString())
                val playlistHolder = holder as PlaylistViewHolder
                playlistHolder.bindPlaylist(item.first as Pair<Contact, ArrayList<Song>>, position)
            }
            TAG_TRACK -> {
                val trackHolder = holder as TrackViewHolder
                trackHolder.bindTrack(item.first as Track, position)
            }
            else -> {
                // nah bruh
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameField : TextView
        val playlistRv : RecyclerView

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
            val playlistSongAdapter = HorizontalPlaylistAdapter(context, songs)
            playlistRv.adapter = playlistSongAdapter
            // LinearLayoutManager.HORIZONTAL,
            //                false
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false)
            playlistRv.layoutManager = linearLayoutManager
        }
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover : SimpleDraweeView
        val songTitle : TextView
        val albumTitle : TextView
        val artistName : TextView

        val heartButton : ImageView
        val addToPlaylistButton : ImageView
        val deleteFromPlaylistButton : ImageView

        init{
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
            deleteFromPlaylistButton = itemView.findViewById(R.id.deleteFromPlaylistButton)
        }

        fun bindTrack(track: Track, position: Int) {
            val trackTitleText = track.name
            songTitle.setText(trackTitleText)

            val albumTitleText = track.album.name
            albumTitle.setText(albumTitleText)

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
                Log.e("HomeFeedItemAdapter", "error: " + e.message)
            }

            listOf(heartButton, deleteFromPlaylistButton, addToPlaylistButton).map{
                button -> button.visibility = View.GONE
            }
        }
    }



}