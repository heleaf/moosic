package com.dev.moosic.viewmodels

import androidx.lifecycle.ViewModel
import com.dev.moosic.models.UserRepositorySong

class SongViewModel : ViewModel() {
    var songs: ArrayList<UserRepositorySong> = ArrayList()
}