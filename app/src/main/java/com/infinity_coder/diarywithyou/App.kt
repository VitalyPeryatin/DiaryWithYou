package com.infinity_coder.diarywithyou

import android.app.Application
import android.os.Environment
import androidx.room.Room
import com.infinity_coder.diarywithyou.data.db.DiaryDatabase
import java.io.File

class App: Application() {
    lateinit var db: DiaryDatabase
    override fun onCreate() {
        super.onCreate()
        instance = this
        db = Room.databaseBuilder(this, DiaryDatabase::class.java, "DiaryDB")
            .fallbackToDestructiveMigration()
            .build()
    }

    fun getRootDir(): String?{
        val folder = File("${Environment.getExternalStorageDirectory()}/${resources.getString(R.string.app_name)}")
        var result = true
        if (!folder.exists()) {
            result = folder.mkdirs()
        }
        return if(result) folder.absolutePath  else null
    }

    companion object {
        lateinit var instance: App
        private set
    }
}