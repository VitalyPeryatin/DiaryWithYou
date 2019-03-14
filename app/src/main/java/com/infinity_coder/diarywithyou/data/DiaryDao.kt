package com.infinity_coder.diarywithyou.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import com.infinity_coder.diarywithyou.domain.DiaryPage

@Dao
interface DiaryDao {
    @Query("select * from diarychapter")
    fun getAllChapters(): LiveData<List<DiaryChapter>>

    @Query("select * from diarychapter where name = :name")
    fun getByName(name: String): DiaryChapter

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChapter(diaryChapter: DiaryChapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPage(diaryPage: DiaryPage)

    @Delete
    fun delete(diaryChapter: DiaryChapter)

    @Query("delete from diarychapter where name = :name")
    fun deleteByName(name: String)

    @Query("select * from diarypage where chapter = :name")
    fun getPagesByChapterName(name: String): LiveData<List<DiaryPage>>
}