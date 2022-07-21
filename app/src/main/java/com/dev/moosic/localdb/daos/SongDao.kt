package com.dev.moosic.localdb.daos

import androidx.room.*
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.localdb.entities.SavedSong

@Dao
interface SongDao {
    @Query("SELECT " + LocalDbUtil.SAVEDSONG_COLUMN_KEY_TRACKJSON + " FROM " +
            LocalDbUtil.SAVEDSONG_TABLE_NAME + " WHERE " +
            LocalDbUtil.SAVEDSONG_COLUMN_KEY_SPOTIFYID + " = :songId"
    )
    fun songInDB(songId: String) : List<String>

    @Insert
    fun insertSongInfo(vararg song: SavedSong)

    @Update
    fun updateSongInfo(vararg song: SavedSong)

    @Delete
    fun deleteSongInfo(vararg song: SavedSong)
}