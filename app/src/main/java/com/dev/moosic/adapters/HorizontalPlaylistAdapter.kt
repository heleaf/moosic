package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.controllers.SongController
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

private const val TAG = "HorizontalPlaylistAdapter"
class HorizontalPlaylistAdapter(context: Context, songs: ArrayList<Song>,
                                miniPlayerController: SongController,
                                playlistController: UserRepoPlaylistControllerInterface)
    : RecyclerView.Adapter<HorizontalPlaylistAdapter.ViewHolder>() {

    val context: Context
    private val songs: ArrayList<Song>
    val miniPlayerController: SongController
    private val playlistController: UserRepoPlaylistControllerInterface

    init {
        this.context = context
        this.songs = songs
        this.miniPlayerController = miniPlayerController
        this.playlistController = playlistController
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(
            R.layout.single_friend_playlist_song_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs.get(position)
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCover: SimpleDraweeView
        private val songTitle: TextView
        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.singleFriendPlaylistItemSongTitle)
        }
        fun bind(song: Song) {
            songTitle.setText(song.getName())
            try {
                val albumCoverImgUri = song.getImageUri()
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                e.message?.let { Log.e(TAG, it) }
            }

            val gson = Gson()
            val track = gson.fromJson(song.getJsonDataString(), Track::class.java)

            itemView.setOnLongClickListener {
                playlistController.addToPlaylist(UserRepositorySong(track.id,
                    gson.toJson(track).toString()), true)
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                miniPlayerController.playSongOnSpotify(track.uri, track.id)
            }

        }
    }

}