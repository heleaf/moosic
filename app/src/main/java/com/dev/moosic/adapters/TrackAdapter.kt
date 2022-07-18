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
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception

private const val TAG = "TopTrackAdapter"
private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class TrackAdapter(context : Context, tracks : ArrayList<Track>,
                   controller : MainActivity.MainActivitySongController)
    : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    var mContext : Context
    var mTracks : ArrayList<Track> = ArrayList()
    val mainActivitySongController : MainActivity.MainActivitySongController = controller
    val adapter = this

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
        var artistName : TextView
        var heartButton : ImageView

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            trackTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
        }
        fun bind(track: Track, position: Int) {
            Log.d(TAG, "binding " + track.name)
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

            var isInPlaylist = false
            mainActivitySongController.isInPlaylist(track, object: Callback<Boolean> {
                override fun success(t: Boolean?, response: Response?) {
                    if (t==null) {heartButton.visibility = View.GONE; return }
                    val heartIcon = if (t == true) R.drawable.ufi_heart_active else R.drawable.ufi_heart
                    heartButton.setImageResource(heartIcon)
                    isInPlaylist = t
                }
                override fun failure(error: RetrofitError?) {
                    heartButton.visibility = View.GONE
                }
            })

            fun removeFromPlaylistSuccess() {
                heartButton.setImageResource(R.drawable.ufi_heart)
                isInPlaylist = !isInPlaylist
            }

            fun addToPlaylistSuccess() {
                heartButton.setImageResource(R.drawable.ufi_heart_active)
                isInPlaylist = !isInPlaylist
            }

            heartButton.setOnClickListener(View.OnClickListener {
                if (isInPlaylist) {
                    mainActivitySongController.removeFromPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            removeFromPlaylistSuccess()
                        }
                        override fun failure(error: RetrofitError?) {}
                    })
                } else {
                    mainActivitySongController.addToPlaylist(track, object: Callback<Unit> {
                        override fun success(t: Unit?, response: Response?) {
                            addToPlaylistSuccess()
                        }
                        override fun failure(error: RetrofitError?) {}

                    })
                }

            })

            itemView.setOnLongClickListener {
                mainActivitySongController.addToParsePlaylist(track, object: Callback<Unit> {
                    override fun success(t: Unit?, response: Response?) {
                        addToPlaylistSuccess()
                    }
                    override fun failure(error: RetrofitError?) {}
                })
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                mainActivitySongController.playSongOnSpotify(track.uri, track.id)
            }

        }

    }
}