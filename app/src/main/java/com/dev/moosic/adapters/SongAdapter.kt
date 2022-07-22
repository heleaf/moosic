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
import com.dev.moosic.Util
import com.dev.moosic.controllers.OldSongController
import com.dev.moosic.controllers.TestSongControllerInterface
import com.dev.moosic.models.Song
import com.dev.moosic.models.UserRepositorySong
import com.facebook.drawee.view.SimpleDraweeView
import com.google.gson.Gson
import com.parse.ParseUser
import kaaes.spotify.webapi.android.models.Track
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.lang.Exception

private const val TAG = "SongAdapter"
private const val EMPTY_STR = ""
private const val ARTIST_STR_SEPARATOR = ", "

class SongAdapter(
    context: Context,
    songs: ArrayList<Song>,
    controller: OldSongController,
    emptyPlaylistText: TextView?,
    testController: TestSongControllerInterface
)
    : RecyclerView.Adapter<SongAdapter.ViewHolder>(){

    var context: Context
    var songs: ArrayList<UserRepositorySong> = ArrayList() //ArrayList<Song> = ArrayList()
    val songController : OldSongController = controller
    var spotifyUserId : String? = null
    var emptyPlaylistText: TextView?

    val testController : TestSongControllerInterface = testController

    init {
        this.context = context
        this.songs = testController.getUserPlaylist()
        // this.songs = songs
        val currentParseUser = ParseUser.getCurrentUser()
        this.spotifyUserId = currentParseUser.getString(Util.PARSEUSER_KEY_SPOTIFY_ACCOUNT_USERNAME)
        this.emptyPlaylistText = emptyPlaylistText
        if (this.songs.isEmpty()) {
            showEmptyPlaylistText()
        }
    }

    fun showEmptyPlaylistText(){
        this.emptyPlaylistText?.visibility = View.VISIBLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(this.context).inflate(
            R.layout.single_track_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = this.songs.get(position)
        holder.bind(song, position)
    }

    override fun getItemCount(): Int {
        return this.songs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumCover : SimpleDraweeView
        var songTitle : TextView
        var artistName : TextView
        var heartButton : ImageView

        init {
            albumCover = itemView.findViewById(R.id.singleFriendPlaylistSongImage)
            songTitle = itemView.findViewById(R.id.trackTitle)
            artistName = itemView.findViewById(R.id.artistName)
            heartButton = itemView.findViewById(R.id.heartButton)
        }

        fun bind(song: UserRepositorySong/*Song*/, position: Int) {
            val jsonDataString = song.trackJsonData
            val gson = Gson()
            val track = gson.fromJson(jsonDataString, Track::class.java)
            val artistNameText = track.artists.fold(
                    EMPTY_STR
                ) { accumulator, artist ->
                    if (artist.name == track.artists.get(0).name) artist.name else
                        accumulator + ARTIST_STR_SEPARATOR + artist.name
                }
                artistName.setText(artistNameText)


            songTitle.setText(track.name)

            itemView.setOnClickListener {
                val id = track.id
                if (id != null){
                    track.uri
                        ?.let { uri -> songController.playSongOnSpotify(uri, id) }
                }
            }

            try {
                val albumCoverImgUri = track.album.images.get(0).url
                albumCover.setImageURI(albumCoverImgUri);
            } catch (e : Exception) {
                e.message?.let { Log.e(TAG, it) }
            }

            heartButton.visibility = View.VISIBLE

            val heartIcon = if (testController.isInPlaylist(track.id)) R.drawable.ufi_heart_active
                            else R.drawable.ufi_heart
            heartButton.setImageResource(heartIcon)

            heartButton.setOnClickListener {
                if (testController.isInPlaylist(track.id)) {
                    testController.removeFromPlaylist(song)
                    this@SongAdapter.notifyItemRemoved(position)
                    this@SongAdapter.notifyItemRangeChanged(position, songs.size)
                    if (songs.isEmpty()) {
                        showEmptyPlaylistText()
                    }
                } else {
                    testController.addToPlaylist(song, true)
                }
            }

        }
    }

}