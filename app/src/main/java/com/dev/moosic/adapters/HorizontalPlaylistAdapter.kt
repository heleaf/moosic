package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import java.lang.Exception

class HorizontalPlaylistAdapter(context: Context, songs: ArrayList<Song>)
    : RecyclerView.Adapter<HorizontalPlaylistAdapter.ViewHolder>() {

    val context: Context
    val songs: ArrayList<Song>

    init {
        this.context = context
        this.songs = songs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.single_friend_playlist_song_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs.get(position)
        holder.bind(song, position)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumCover: SimpleDraweeView
        val songTitle: TextView
        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.singleFriendPlaylistItemSongTitle)
        }
        fun bind(song: Song, position: Int) {
            songTitle.setText(song.getName())
            try {
                val albumCoverImgUri = song.getImageUri()
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                Log.e("SongAdapter", "error: " + e.message)
            }
        }
    }

}