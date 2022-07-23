package com.dev.moosic.localdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dev.moosic.localdb.daos.UserDao

import com.dev.moosic.localdb.entities.SavedUser

@Database(entities = [SavedUser::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}