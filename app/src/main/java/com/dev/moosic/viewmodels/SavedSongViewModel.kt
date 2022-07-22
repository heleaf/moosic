package com.dev.moosic.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dev.moosic.localdb.LocalDatabase

class SavedSongViewModel(val database: LocalDatabase,
application: Application) : AndroidViewModel(application) {

}