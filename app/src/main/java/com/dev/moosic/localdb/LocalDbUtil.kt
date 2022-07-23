package com.dev.moosic.localdb

class LocalDbUtil {
    companion object {
        const val DATABASE_NAME = "local-cache-db"

        const val SAVEDUSER_TABLE_NAME = "saveduser"
        const val SAVEDUSER_COLUMN_KEY_PARSEUSER_ID = "parseUserId"
        const val SAVEDUSER_COLUMN_KEY_CACHED_SONGS = "cachedSongs"
    }
}