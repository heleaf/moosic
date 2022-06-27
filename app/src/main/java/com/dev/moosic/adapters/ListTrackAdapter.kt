package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.dev.moosic.models.Song
import com.facebook.drawee.view.SimpleDraweeView
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

class ListTrackAdapter(context: Context, tracks : List<Track>, controller: MainActivity.MainActivityController,
showAddButton : Boolean, showDeleteButton : Boolean) : PagedListAdapter <Track, ListTrackAdapter.ViewHolder>(DIFF_CALLBACK) {
    val TAG = "ListSongAdapter"

    var mContext : Context? = null
    var mTracks : List<Track> = ArrayList()
    val mUserSpotifyId : String? = ParseUser.getCurrentUser().getString("userId")
    val mPlaylistId : String? = ParseUser.getCurrentUser().getString("playlistId")

    val mainActivityController : MainActivity.MainActivityController = controller
    val mShowAddButton = showAddButton
    val mShowDeleteButton = showDeleteButton

    init {
        this.mContext = context
        this.mTracks = tracks // addAll?
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView? = null
        var trackTitle : TextView? = null
        var albumTitle : TextView? = null
        var artistName : TextView? = null

        var heartButton : ImageView? = null
        var addToPlaylistButton : ImageView? = null

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
//                updateTrackLikedStatus(track, heartButton!!)
//                val isLiked = mainActivityController.tracksAreSaved(listOf(track))
                mainActivityController.addToSavedTracks(track.id)
            })

            if (mShowAddButton) {
                addToPlaylistButton?.visibility = View.VISIBLE
                addToPlaylistButton?.setOnClickListener(View.OnClickListener {
//                    addTrackToPlaylist(track)
                    mainActivityController.addToPlaylist(this@ListTrackAdapter.mUserSpotifyId!!,
                        this@ListTrackAdapter.mPlaylistId!!, track)
                })
            } else {
                addToPlaylistButton?.visibility = View.GONE
            }

            val deleteButton : ImageView = itemView.findViewById(R.id.deleteFromPlaylistButton)
            if (mShowDeleteButton){
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "deleting " + track.name + " from playlist")
                    mainActivityController.removeFromPlaylist(this@ListTrackAdapter.mUserSpotifyId!!,
                        this@ListTrackAdapter.mPlaylistId!!, track, position)
                    // mTracks.remove(track) // i shouldn't need to do this...?
                    this@ListTrackAdapter.notifyItemRemoved(position)
                    this@ListTrackAdapter.notifyItemRangeChanged(position, mTracks.size);
                })
            } else {
                deleteButton.visibility = View.GONE
            }
        }

        // TODO: pick between playlists
        // fully implement likes

    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Track> = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.id == newItem.id // same spotify id
            }
            override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }

//    fun addMoreTracks(newTracks: List<Track>) {
//        tracks.addAll(newTracks)
//        submitList(tracks) // DiffUtil takes care of the check
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(R.layout.single_track_item,
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position) ?: return
        // TODO: non-null handling
        holder.bind(song, position)
    }

}