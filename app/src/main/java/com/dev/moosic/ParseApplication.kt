package com.dev.moosic

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import com.parse.Parse
import com.parse.ParseObject

class ParseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.networkInterceptors().add(httpLoggingInterceptor)


        // set applicationId, and server server based on the values in the back4app settings.
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("OcxXVntOKmUYQP34WgKbwvF0YXCJUj1d2vkBiLr6") // should correspond to Application Id env variable
                .clientKey("VQmtewq3fK5NTZm5iBFwTKEtxp2NXjZL69BeV7CX") // should correspond to Client key env variable
                .server("https://parseapi.back4app.com").build()
        )

        Fresco.initialize(this);

    }
}