package com.dev.moosic.localdb

class LocalDbUtil {
    companion object {
        const val DATABASE_NAME = "local-cache-db"

        const val SAVEDSONG_TABLE_NAME = "savedsong"
        const val SAVEDSONG_COLUMN_KEY_TITLE = "title"
        const val SAVEDSONG_COLUMN_KEY_ARTISTS = "artists"
        const val SAVEDSONG_COLUMN_KEY_IMAGEURI = "imageUri"
        const val SAVEDSONG_COLUMN_KEY_SPOTIFYID = "spotifyId"
        const val SAVEDSONG_COLUMN_KEY_TRACKJSON = "trackJsonDataStr"
        const val SAVEDSONG_COLUMN_KEY_DATEADDEDAT = "dateAddedAt"

        const val SAVEDUSER_TABLE_NAME = "saveduser"
        const val SAVEDUSER_COLUMN_KEY_PARSEUSER_ID = "parseUserId"
        const val SAVEDUSER_COLUMN_KEY_CACHED_SONGS = "cachedSongs"
    }
}