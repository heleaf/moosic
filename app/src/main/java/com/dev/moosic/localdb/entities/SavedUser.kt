package com.dev.moosic.localdb.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dev.moosic.localdb.LocalDbUtil

@Entity
data class SavedUser(
    @PrimaryKey val parseUsername : String,
    @ColumnInfo(name = LocalDbUtil.SAVEDUSER_COLUMN_KEY_PARSEUSER_ID) val parseUserId : String,
    @ColumnInfo(name = LocalDbUtil.SAVEDUSER_COLUMN_KEY_CACHED_SONGS) val savedSongs : String
    )
