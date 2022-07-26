package com.dev.moosic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.room.Room
import com.dev.moosic.controllers.SettingsController
import com.dev.moosic.fragments.MixedHomeFeedFragment
import com.dev.moosic.fragments.OfflineHomeFeedFragment
import com.dev.moosic.fragments.SettingsFragment
import com.dev.moosic.localdb.LocalDatabase
import com.dev.moosic.localdb.LocalDbUtil
import com.dev.moosic.localdb.daos.UserDao
import com.dev.moosic.models.UserRepositorySong
import com.dev.moosic.viewmodels.SavedSongsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response

private const val TOAST_USING_IN_OFFLINE_MODE = "Warning: you are using the app in offline mode"
private const val TAG = "OfflineMainActivity"

class OfflineMainActivity : AppCompatActivity() {

    lateinit var bottomNavBar: BottomNavigationView
    val songs: ArrayList<UserRepositorySong> = ArrayList()
    var showingHome = true
    lateinit var loadingBar: ProgressBar

    private lateinit var db : LocalDatabase

    private val savedSongModel = SavedSongsViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_offline_main)

        db = Room.databaseBuilder(
            applicationContext,
            LocalDatabase::class.java, LocalDbUtil.DATABASE_NAME
        ).build()

        Util.extractTopTenSongs(savedSongModel, db) {
            _ -> if (showingHome) goToOfflineHomeFragment()
        }

        val toolbar = findViewById<Toolbar>(R.id.offlineMainToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.stat_sys_headset)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        bottomNavBar = findViewById(R.id.offlineBottomNavBar)
        bottomNavBar.setOnItemSelectedListener {  menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.offlineHomeMenuItem -> { goToOfflineHomeFragment() }
                R.id.offlineSettingsMenuItem -> { goToOfflineSettingsMenu() }
            }
            return@setOnItemSelectedListener true
        }

        loadingBar = findViewById(R.id.offlineLoading)
        loadingBar.visibility = View.INVISIBLE
        Toast.makeText(this, TOAST_USING_IN_OFFLINE_MODE,
            Toast.LENGTH_LONG).show()
    }

    private fun goToOfflineHomeFragment() {
        showingHome = true
        songs.clear()
        songs.addAll(savedSongModel.songs)
        if (songs.size > 0) {
            val fragment = OfflineHomeFeedFragment.newInstance(songs)
            supportFragmentManager.beginTransaction()
                .replace(R.id.offlineFlContainer, fragment).commit()
            return
        }
    }

    private fun goToOfflineSettingsMenu() {
        showingHome = false
        val fragment = SettingsFragment.newInstance(SettingsController(this))
        supportFragmentManager.beginTransaction()
            .replace(R.id.offlineFlContainer, fragment).commit()
    }

}