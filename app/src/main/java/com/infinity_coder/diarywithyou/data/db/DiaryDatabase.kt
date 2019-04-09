package com.infinity_coder.diarywithyou.data.db

import androidx.room.Database
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import com.infinity_coder.diarywithyou.data.DiaryDao
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration



@Database(entities = [DiaryChapter::class, DiaryPage::class], version = 2)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS  DiaryChapter")
                database.execSQL("CREATE TABLE DiaryChapter (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, pdfPath TEXT NOT NULL, coverPath TEXT)")
            }
        }
    }
}