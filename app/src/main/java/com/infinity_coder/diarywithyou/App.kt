package com.infinity_coder.diarywithyou

import android.app.Application
import androidx.room.Room
import com.infinity_coder.diarywithyou.domain.DiaryDatabase

class App: Application() {
    lateinit var db: DiaryDatabase
    override fun onCreate() {
        super.onCreate()
        instance = this
        db = Room.databaseBuilder(this, DiaryDatabase::class.java, "DiaryDB").build()
    }

    companion object {
        lateinit var instance: App
        private set
    }
}