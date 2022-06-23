package com.dev.moosic.models

import com.spotify.protocol.types.ListItem
import kaaes.spotify.webapi.android.models.Track
import org.parceler.Parcel

@Parcel
// TODO: make into parse object to update database
class Song () {
    var mId : String? = null
    var mTitle : String? = null
    var mAlbum : SongAlbum? = null
    var mArtist : List<SongArtist>? = null
    var mUri : String? = null
    var mImgUri : String? = null

    companion object Factory {
//        fun fromListItem(listItem: ListItem) : Song? {
//            if (listItem.hasChildren) return null // not a valid track
//            var song = Song()
//            song.mId = listItem.id
//            song.mTitle = listItem.title
//            song.mSubtitle = listItem.title
//            song.mUri = listItem.uri
//            song.mImgUri = listItem.imageUri.raw
//            val trackFields = listOf(song.mId, song.mTitle, song.mSubtitle, song.mUri, song.mImgUri)
//            val hasNull = trackFields.fold(false, {acc, elem -> (elem == null) or acc})
//            return if (hasNull) null else song
//        }
//
//        fun fromTrack(track: Track) : Song? {
//            var song = Song()
//            song.mId = track.id
//            song.mTitle = track.name
//            song.mArtist = SongArtist.fromArtists(track.artists)
//            song.mAlbum = SongAlbum.fromAlbum(track.album)
//            song.mUri = track.uri
//            song.mImgUri = if (track.album.images.size > 0) track.album.images.get(0).url else ""
//            val songFields = listOf(song.mId, song.mTitle, song.mArtist, song.mAlbum, song.mUri, song.mImgUri)
//            val hasNull = songFields.fold(false, {acc, elem -> (elem == null) or acc})
//            return if (hasNull) null else song
//        }
//
//        fun fromTracks(tracks: List<Track>) : List<Song> {
//            val songs = (1..(tracks.size-1)).map{ index ->
//                val track = tracks.get(index)
//                val song = Song.fromTrack(track)
//                song
//            }
//            val nonNullSongs = songs.filterNotNull()
//            return nonNullSongs
//        }
    }

}