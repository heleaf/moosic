package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

class TopTrackAdapter(context : Context, tracks : List<Track>, userId : String, playlistId : String,
controller : MainActivity.MainActivityController, showAddButton : Boolean, showDeleteButton : Boolean)
    : RecyclerView.Adapter<TopTrackAdapter.ViewHolder>() {
    val TAG = "TopTrackAdapter"
    var mContext : Context? = null
    var mTracks : ArrayList<Track> = ArrayList()
    var mUserId : String? = null
    var mPlaylistId : String? = null
    val mainActivityController : MainActivity.MainActivityController = controller

    val mShowDeleteButton = showDeleteButton
    val mShowAddButton = showAddButton

    init {
        this.mContext = context
        this.mTracks.addAll(tracks)
        this.mUserId = userId
        this.mPlaylistId = playlistId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopTrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopTrackAdapter.ViewHolder, position: Int) {
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
            albumCover = itemView.findViewById(R.id.topTrackImg)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
        }
        fun bind(track: Track, position: Int) {
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

            heartButton?.setOnClickListener(View.OnClickListener {
                updateTrackLikedStatus(track, heartButton!!)
            })

            if (mShowAddButton) {
                addToPlaylistButton?.visibility = View.VISIBLE
                addToPlaylistButton?.setOnClickListener(View.OnClickListener {
                    addTrackToPlaylist(track)
                    mainActivityController.addToPlaylist(this@TopTrackAdapter.mUserId!!,
                        this@TopTrackAdapter.mPlaylistId!!, track)
                })
            } else {
                addToPlaylistButton?.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "deleting " + track.name + " from playlist")
                     mainActivityController.removeFromPlaylist(this@TopTrackAdapter.mUserId!!,
                         this@TopTrackAdapter.mPlaylistId!!, track, position)
//                    mTracks.remove(track) // i shouldn't need to do this...
                    mTracks.removeAt(position)
                    this@TopTrackAdapter.notifyItemRemoved(position)
                    this@TopTrackAdapter.notifyItemRangeChanged(position, mTracks.size);
                })
            } else {
                deleteButton.visibility = View.GONE
            }
        }

        // TODO: pick between playlists
        private fun addTrackToPlaylist(track: Track) {
            Log.d(TAG, "adding " + track.name + " to playlist")
        }

        private fun updateTrackLikedStatus(track: Track, heartButton: ImageView) {
            Log.d(TAG, "updating liked status of " + track.name)
            // if it is liked, mSpotifyApi.service.addToMySavedTracks()
            // if it is not liked, mSpotifyApi.service.removeFromMySavedTracks()
        }

    }
}