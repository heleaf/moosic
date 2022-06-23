package com.dev.moosic.models

import kaaes.spotify.webapi.android.models.Album
import kaaes.spotify.webapi.android.models.AlbumSimple

// TODO: make into parse object to update database
class SongAlbum {

    var mId : String? = null
    var mTitle : String? = null
    var mArtist : String? = null
    var mSongs : List<Song>? = null
    var mUri : String? = null
    var mImgUri : String? = null

    companion object Factory {
//        fun fromAlbum(album: AlbumSimple) : SongAlbum? {
//            var songAlbum = SongAlbum()
//            songAlbum.mId = album.id
//            songAlbum.mTitle = album.name
//            songAlbum.mArtist = SongArtist.fromArtists(album.)
//            songAlbum.mSongs = Song.fromTracks(album)
//            songAlbum.mUri = album.uri
//            songAlbum.mImgUri = if (album.images.size > 0) album.images.get(0).url else ""
//            val albumFields = listOf(songAlbum.mId, songAlbum.mTitle, songAlbum.mArtist, songAlbum.mUri, songAlbum.mImgUri)
//            val hasNull = albumFields.fold(false, {acc, elem -> (elem == null) or acc})
//            return if (hasNull) null else songAlbum
//        }
    }
}