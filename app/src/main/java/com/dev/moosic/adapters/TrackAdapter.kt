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

class TrackAdapter(context : Context, tracks : ArrayList<Track>, userId : String, playlistId : String,
                   controller : MainActivity.MainActivitySongController, showAddButton : Boolean, showDeleteButton : Boolean)
    : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    val TAG = "TopTrackAdapter"
    var mContext : Context? = null
    var mTracks : ArrayList<Track> = ArrayList()
    var mUserId : String? = null
    var mPlaylistId : String? = null
    val mainActivitySongController : MainActivity.MainActivitySongController = controller

    var mShowDeleteButton = showDeleteButton
    var mShowAddButton = showAddButton

    init {
        this.mContext = context
        this.mTracks = tracks
        this.mUserId = userId
        this.mPlaylistId = playlistId

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(R.layout.single_track_item, parent, false)
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
        var albumCover : SimpleDraweeView? = null
        var trackTitle : TextView? = null
        var albumTitle : TextView? = null
        var artistName : TextView? = null

        var heartButton : ImageView? = null
        var addToPlaylistButton : ImageView? = null

        val TAG = "TopTrackAdapter"

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
                Log.d(TAG, "adding $track to playlist")
                mainActivitySongController.addToParsePlaylist(track)
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                mainActivitySongController.playSongOnSpotify(track.uri, track.id)
            }

            val trackTitleText = track.name
            trackTitle?.setText(trackTitleText)

            val albumTitleText = track.album.name
            albumTitle?.setText(albumTitleText)

            val artistNameText = track.artists.fold(
                ""
            ) { accumulator, artist ->
                if (artist.name == track.artists.get(0).name) artist.name else
                    accumulator + ", " + artist.name
            }
            artistName?.setText(artistNameText)

            try {
                val albumCoverImgUri = track.album.images.get(0).url
                albumCover?.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                Log.e(TAG, "error: " + e.message)
            }

            heartButton?.visibility = View.GONE
            heartButton?.setOnClickListener(View.OnClickListener {
                updateTrackLikedStatus(track, heartButton!!)
                mainActivitySongController.addToSavedTracks(track.id)
            })

            if (mShowAddButton) {
                addToPlaylistButton?.visibility = View.VISIBLE
                addToPlaylistButton?.setOnClickListener(View.OnClickListener {
                    mainActivitySongController.addToPlaylist(this@TrackAdapter.mUserId!!,
                        this@TrackAdapter.mPlaylistId!!, track)
                })
            } else {
                addToPlaylistButton?.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "deleting " + track.name + " from playlist")
                     mainActivitySongController.removeFromPlaylist(this@TrackAdapter.mUserId!!,
                         this@TrackAdapter.mPlaylistId!!, track, position)
                    mTracks.removeAt(position)
                    this@TrackAdapter.notifyItemRemoved(position)
                    this@TrackAdapter.notifyItemRangeChanged(position, mTracks.size);
                })
            } else {
                deleteButton.visibility = View.GONE
            }
        }

        // TODO
        private fun updateTrackLikedStatus(track: Track, heartButton: ImageView) {
            Log.d(TAG, "updating liked status of " + track.name)
        }

    }
}