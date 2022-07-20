package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.controllers.OldSongController
import com.dev.moosic.controllers.TestSongControllerInterface
import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception

private const val TAG = "HorizontalPlaylistAdapter"
class HorizontalPlaylistAdapter(context: Context, songs: ArrayList<Song>, controller: OldSongController,
    testSongController: TestSongControllerInterface)
    : RecyclerView.Adapter<HorizontalPlaylistAdapter.ViewHolder>() {

    val context: Context
    private val songs: ArrayList<Song>
    val controller: OldSongController
    private val testSongController: TestSongControllerInterface

    init {
        this.context = context
        this.songs = songs
        this.controller = controller
        this.testSongController = testSongController
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
//                  testSongController.addToPlaylist(UserRepositorySong(song.getSpotifyId()!!,
//                    song.getJsonDataString()!!))

                controller.addToPlaylist(track, object: Callback<Unit> {
                    override fun success(t: Unit?, response: Response?) {
                    }
                    override fun failure(error: RetrofitError?) {
                    }
                })
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                controller.playSongOnSpotify(track.uri, track.id)
            }

        }
    }

}