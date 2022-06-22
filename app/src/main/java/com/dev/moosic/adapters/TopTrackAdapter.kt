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
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.models.Track
import java.lang.Exception

class TopTrackAdapter(context : Context, tracks : List<Track>) : RecyclerView.Adapter<TopTrackAdapter.ViewHolder>() {
    var mContext : Context? = null
    var mTracks : List<Track> = ArrayList()

    init {
        this.mContext = context
        this.mTracks = tracks
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopTrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(R.layout.top_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopTrackAdapter.ViewHolder, position: Int) {
        val track = this.mTracks.get(position)
        holder.bind(track)
    }

    override fun getItemCount(): Int {
        return this.mTracks.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView? = null
        var trackTitle : TextView? = null
        var albumTitle : TextView? = null
        var artistName : TextView? = null

        val TAG = "TopTrackAdapter"

        init {
            albumCover = itemView.findViewById(R.id.topTrackImg)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
        }
        fun bind(track: Track) {
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



        }

    }
}