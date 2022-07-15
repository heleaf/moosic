package com.dev.moosic

import android.app.Application
import com.dev.moosic.models.Playlist
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.facebook.drawee.backends.pipeline.Fresco
import com.parse.Parse
import com.parse.ParseObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

private const val PARSE_APPLICATION_ID = "OcxXVntOKmUYQP34WgKbwvF0YXCJUj1d2vkBiLr6"
private const val PARSE_CLIENT_KEY = "VQmtewq3fK5NTZm5iBFwTKEtxp2NXjZL69BeV7CX"
private const val PARSE_SERVER_URL = "https://parseapi.back4app.com"

class ParseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.networkInterceptors().add(httpLoggingInterceptor)

        ParseObject.registerSubclass(Playlist::class.java)
        ParseObject.registerSubclass(Song::class.java)
        ParseObject.registerSubclass(SongFeatures::class.java)

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(PARSE_APPLICATION_ID)
                .clientKey(PARSE_CLIENT_KEY)
                .server(PARSE_SERVER_URL).build()
        )

        Fresco.initialize(this);
    }
}