package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

private const val TAG = "TopTrackAdapter"
private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class TrackAdapter(context : Context, tracks : ArrayList<Track>,
                   controller : MainActivity.MainActivitySongController,
                   showAddButton : Boolean, showDeleteButton : Boolean)
    : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    var mContext : Context
    var mTracks : ArrayList<Track> = ArrayList()
    val mainActivitySongController : MainActivity.MainActivitySongController = controller
    var mShowDeleteButton = showDeleteButton
    var mShowAddButton = showAddButton

    init {
        this.mContext = context
        this.mTracks = tracks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.mContext)
            .inflate(R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackAdapter.ViewHolder, position: Int) {
        val track = this.mTracks.get(position)
        holder.bind(track, position)
    }

    override fun getItemCount(): Int {
        return this.mTracks.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView
        var trackTitle : TextView
        var albumTitle : TextView
        var artistName : TextView
        var heartButton : ImageView
        var addToPlaylistButton : ImageView

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
        }
        fun bind(track: Track, position: Int) {
            itemView.setOnLongClickListener {
                mainActivitySongController.addToParsePlaylist(track)
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                mainActivitySongController.playSongOnSpotify(track.uri, track.id)
            }

            val trackTitleText = track.name
            trackTitle.setText(trackTitleText)

            val albumTitleText = track.album.name
            albumTitle.setText(albumTitleText)

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

            heartButton.visibility = View.GONE
            heartButton.setOnClickListener(View.OnClickListener {
                mainActivitySongController.addToSavedTracks(track.id)
            })

            if (mShowAddButton) {
                addToPlaylistButton.visibility = View.VISIBLE
                addToPlaylistButton.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.addToPlaylist(track)
                })
            } else {
                addToPlaylistButton.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.removeFromPlaylist(track, position)
                    mTracks.removeAt(position)
                    this@TrackAdapter.notifyItemRemoved(position)
                    this@TrackAdapter.notifyItemRangeChanged(position, mTracks.size);
                })
            } else {
                deleteButton.visibility = View.GONE
            }
        }

    }
}