package com.dev.moosic.localdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev.moosic.localdb.LocalDbUtil
import java.util.*

@Entity
data class SavedSong(
    @PrimaryKey val songId: Int,
    @ColumnInfo(name = LocalDbUtil.SAVEDSONG_COLUMN_KEY_TITLE) val title: String,
    @ColumnInfo(name = LocalDbUtil.SAVEDSONG_COLUMN_KEY_ARTISTS) val artists: String,
    @ColumnInfo(name = LocalDbUtil.SAVEDSONG_COLUMN_KEY_IMAGEURI) val imageUri: String,
    @ColumnInfo(name = LocalDbUtil.SAVEDSONG_COLUMN_KEY_SPOTIFYID) val spotifyId: String,
    @ColumnInfo(name = LocalDbUtil.SAVEDSONG_COLUMN_KEY_TRACKJSON) val trackJsonDataStr: String,
    )
