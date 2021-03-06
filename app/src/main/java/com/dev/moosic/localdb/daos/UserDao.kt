package com.dev.moosic.localdb.daos

import androidx.room.*
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.localdb.entities.SavedUser

@Dao
interface UserDao {
    @Query("SELECT ${LocalDbUtil.SAVEDUSER_COLUMN_KEY_CACHED_SONGS} FROM " +
            LocalDbUtil.SAVEDUSER_TABLE_NAME + " WHERE " +
            "parseUsername" + " = :username"
    )
    suspend fun getUserSavedSongs(username: String): List<String>

    @Query("SELECT * FROM " + LocalDbUtil.SAVEDUSER_TABLE_NAME + " WHERE "  +
            "parseUsername" + " = :username")
    suspend fun getUser(username: String) : SavedUser

    @Insert
    suspend fun insertUserInfo(vararg user: SavedUser)

    @Update
    suspend fun updateUserInfo(vararg user: SavedUser)

    @Delete
    suspend fun deleteUserInfo(vararg user: SavedUser)
}