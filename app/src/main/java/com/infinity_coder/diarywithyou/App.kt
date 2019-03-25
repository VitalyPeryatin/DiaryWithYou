package com.infinity_coder.diarywithyou

import android.app.Application
import android.os.Environment
import androidx.room.Room
import com.infinity_coder.diarywithyou.data.DiaryDao
import com.infinity_coder.diarywithyou.data.db.DiaryDatabase
import com.infinity_coder.diarywithyou.presentation.DIARY_DB_NAME
import java.io.File

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        val db = Room.databaseBuilder(this, DiaryDatabase::class.java, DIARY_DB_NAME)
            .fallbackToDestructiveMigration()
            .build()
        diaryDao = db.diaryDao()
    }

    /**
     * Возвращает путь к корневой директории, где сохраняются PDF файлы
     */
    fun getRootDir(): String?{
        val folder = File("${Environment.getExternalStorageDirectory()}/${resources.getString(R.string.app_name)}")
        var result = true
        if (!folder.exists())
            result = folder.mkdirs()
        return if(result) folder.absolutePath  else null
    }

    companion object {
        lateinit var instance: App
            private set

        lateinit var diaryDao: DiaryDao
            private set
    }
}