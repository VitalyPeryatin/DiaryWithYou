package com.infinity_coder.diarywithyou.domain

import androidx.room.Database
import androidx.room.RoomDatabase
import com.infinity_coder.diarywithyou.data.DiaryDao

@Database(entities = [DiaryChapter::class], version = 1)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}