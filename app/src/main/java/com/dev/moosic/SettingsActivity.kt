package com.dev.moosic

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dev.moosic.controllers.SettingsController
import com.dev.moosic.controllers.SettingsControllerInterface
import com.dev.moosic.fragments.SettingsFragment
import com.parse.ParseUser

class SettingsActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener {
            setResult(Util.RESULT_CODE_EXIT_SETTINGS)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        val settingsFragment = SettingsFragment.newInstance(SettingsController(this))
        supportFragmentManager
            .beginTransaction().replace(R.id.settingsFlContainer, settingsFragment).commit()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}