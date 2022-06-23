package com.dev.moosic.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.dev.moosic.MainActivity
import com.dev.moosic.R
import com.facebook.drawee.view.SimpleDraweeView
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.Pager
import kaaes.spotify.webapi.android.models.PlaylistTrack
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception
import kotlin.contracts.contract

class TopTrackAdapter(context : Context, tracks : List<Track>, userId : String, playlistId : String,
controller : MainActivity.MainActivityController)
    : RecyclerView.Adapter<TopTrackAdapter.ViewHolder>() {
    val TAG = "TopTrackAdapter"
    var mContext : Context? = null
    var mTracks : List<Track> = ArrayList()
    var mUserId : String? = null
    var mPlaylistId : String? = null
    val mainActivityController : MainActivity.MainActivityController = controller

    var onAddToPlaylistClickListener : OnAddToPlaylistClickListener? = null

    init {
        this.mContext = context
        this.mTracks = tracks
        this.mUserId = userId
        this.mPlaylistId = playlistId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopTrackAdapter.ViewHolder {
        val view = LayoutInflater.from(this.mContext).inflate(R.layout.top_track_item, parent, false)
        return ViewHolder(view, this.mUserId!!, this.mPlaylistId!!, mainActivityController)
    }

    override fun onBindViewHolder(holder: TopTrackAdapter.ViewHolder, position: Int) {
        val track = this.mTracks.get(position)
        holder.bind(track)
    }

    override fun getItemCount(): Int {
        return this.mTracks.size
    }

    interface OnAddToPlaylistClickListener {
        fun onAddToPlaylistClickListener(itemView : View, position : Int);
    }

    @JvmName("setOnAddToPlaylistClickListener1")
    fun setOnAddToPlaylistClickListener(listener : OnAddToPlaylistClickListener) {
        this.onAddToPlaylistClickListener = listener
    }

    companion object Factory {
        val TAG = "TopTrackAdapter Factory"
        fun getOnAddToPlaylistClickListener(spotifyApi : SpotifyApi, userId: String, playlistId: String, tracks: List<Track>) :
                OnAddToPlaylistClickListener {
            return object : OnAddToPlaylistClickListener {
                override fun onAddToPlaylistClickListener(itemView: View, position: Int) {
                    val track = tracks[position]
                    val queryParams : Map<String, Any> = emptyMap()
                    val bodyParams : Map<String, Any> = emptyMap()
                    spotifyApi.service.addTracksToPlaylist(userId, playlistId, queryParams, bodyParams, object: Callback<Pager<PlaylistTrack>>{
                        override fun success(t: Pager<PlaylistTrack>?, response: Response?) {
                            Log.d(TAG, "added: " + track.name + " to playlist " + playlistId)
                        }

                        override fun failure(error: RetrofitError?) {
                            Log.d(TAG, "bad request: " + error?.message)
                        }

                    })
                }
            }
        }
    }



    class ViewHolder(itemView: View, userId: String, playlistId: String, controller: MainActivity.MainActivityController) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView? = null
        var trackTitle : TextView? = null
        var albumTitle : TextView? = null
        var artistName : TextView? = null

        var heartButton : ImageView? = null
        var addToPlaylistButton : ImageView? = null
        var mainActivityController = controller

        var mUserId = userId
        var mPlaylistId = playlistId

        val TAG = "TopTrackAdapter"

        init {
            albumCover = itemView.findViewById(R.id.topTrackImg)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            albumTitle = itemView.findViewById(R.id.albumTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
            addToPlaylistButton = itemView.findViewById(R.id.addToPlaylistButton)
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

            heartButton?.setOnClickListener(View.OnClickListener {
                updateTrackLikedStatus(track, heartButton!!)
            })

            addToPlaylistButton?.setOnClickListener(View.OnClickListener {
                addTrackToPlaylist(track)
                mainActivityController.addToPlaylist(mUserId, mPlaylistId, track)
            })

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