package com.dev.moosic

import android.app.Application
import com.dev.moosic.models.Playlist
import com.dev.moosic.models.Song
import com.dev.moosic.models.SongFeatures
import com.dev.moosic.models.TaggedContactList
import com.facebook.drawee.backends.pipeline.Fresco
import com.parse.Parse
import com.parse.ParseObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

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
//        ParseObject.registerSubclass(TaggedContactList::class.java)

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("OcxXVntOKmUYQP34WgKbwvF0YXCJUj1d2vkBiLr6")
                .clientKey("VQmtewq3fK5NTZm5iBFwTKEtxp2NXjZL69BeV7CX")
                .server("https://parseapi.back4app.com").build()
        )

        Fresco.initialize(this);

    }
}