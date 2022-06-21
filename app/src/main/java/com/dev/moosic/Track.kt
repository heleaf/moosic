package com.dev.moosic

import com.spotify.protocol.types.ListItem
import org.parceler.Parcel

@Parcel
class Track () {
    var mId : String? = null
    var mTitle : String? = null
    var mSubtitle : String? = null
    var mUri : String? = null
    var mImgUri : String? = null

    companion object Factory {
        fun fromListItem(listItem: ListItem) : Track? {
            if (listItem.hasChildren) return null // not a valid track
            var track = Track()
            track.mId = listItem.id
            track.mTitle = listItem.title
            track.mSubtitle = listItem.title
            track.mUri = listItem.uri
            track.mImgUri = listItem.imageUri.raw
            val trackFields = listOf(track.mId, track.mTitle, track.mSubtitle, track.mUri, track.mImgUri)
            val hasNull = trackFields.fold(false, {acc, elem -> (elem == null) or acc})
            return if (hasNull) null else track
        }
    }

}