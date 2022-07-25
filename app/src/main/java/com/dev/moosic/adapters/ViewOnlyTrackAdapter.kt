package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

class ViewOnlyTrackAdapter(private val context: Context,
                           private val songs: ArrayList<UserRepositorySong>)
    : RecyclerView.Adapter<ViewOnlyTrackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs.get(position)
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return this.songs.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCover : SimpleDraweeView
        private val songTitle : TextView
        private val artistName : TextView

        private val heartButton : ImageView

        init{
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            heartButton.visibility = View.GONE
        }

        fun bind(song: UserRepositorySong) {
            val gson = Gson()
            val track = gson.fromJson(song.trackJsonData, Track::class.java)
            songTitle.setText(track.name)
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
            } catch (e : Exception) { }
        }

    }
}