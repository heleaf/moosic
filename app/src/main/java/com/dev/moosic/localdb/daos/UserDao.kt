package com.dev.moosic.localdb.daos

import androidx.room.*
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.localdb.entities.SavedSong
import com.dev.moosic.localdb.entities.SavedUser

@Dao
interface UserDao {
    @Query("SELECT ${LocalDbUtil.SAVEDUSER_COLUMN_KEY_CACHED_SONGS} FROM " +
            LocalDbUtil.SAVEDUSER_TABLE_NAME + " WHERE " +
            LocalDbUtil.SAVEDUSER_COLUMN_KEY_PARSEUSER_ID + " = :username"
    )
    fun getUserSavedSongs(username: String): List<String>

    @Insert
    fun insertUserInfo(vararg user: SavedUser)

    @Update
    fun updateUserInfo(vararg user: SavedUser)

    @Delete
    fun deleteUserInfo(vararg user: SavedUser)
}