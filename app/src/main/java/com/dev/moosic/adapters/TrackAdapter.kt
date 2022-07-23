package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.R
import com.dev.moosic.controllers.MainActivityControllerInterface
import com.dev.moosic.controllers.UserRepoPlaylistControllerInterface
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

private const val TAG = "TrackAdapter"
private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class TrackAdapter(context : Context, tracks : ArrayList<Track>,
                   private val miniPlayerController : MainActivityControllerInterface,
                   private val playlistController: UserRepoPlaylistControllerInterface)
    : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    var context : Context
    var tracks : ArrayList<Track> = ArrayList()
    val adapter = this

    init {
        this.context = context
        this.tracks = tracks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.context)
            .inflate(R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackAdapter.ViewHolder, position: Int) {
        val track = this.tracks.get(position)
        holder.bind(track, position)
    }

    override fun getItemCount(): Int {
        return this.tracks.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView
        var trackTitle : TextView
        var artistName : TextView
        var heartButton : ImageView

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
        }
        fun bind(track: Track, position: Int) {
            val trackTitleText = track.name
            trackTitle.setText(trackTitleText)

            val artistNameText = track.artists.fold(
                EMPTY_STR
            ) { accumulator, artist ->
                if (artist.name == track.artists.get(0).name) artist.name else
                    accumulator + ARTIST_STR_SEPARATOR + artist.name
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
                    playlistController.addToPlaylist(
                        UserRepositorySong(track.id,
                        gson.toJson(track).toString()), true)
                    heartButton.setImageResource(R.drawable.ufi_heart_active)
                }
            }

            itemView.setOnLongClickListener {
                val gson = Gson()
                playlistController.addToPlaylist(
                    UserRepositorySong(track.id,
                    gson.toJson(track).toString()), true)
                heartButton.setImageResource(R.drawable.ufi_heart_active)
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                miniPlayerController.playSongOnSpotify(track.uri, track.id)
            }

        }

    }
}