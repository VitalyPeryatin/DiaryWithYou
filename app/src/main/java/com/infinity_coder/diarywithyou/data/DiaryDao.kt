package com.infinity_coder.diarywithyou.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import androidx.room.Transaction
import com.infinity_coder.diarywithyou.data.db.CoverCard


@Dao
interface DiaryDao {
    @Query("select * from diarychapter")
    fun getAllChapters(): LiveData<List<DiaryChapter>>

    @Query("select * from diarychapter where name = :name")
    fun getChapterByName(name: String): DiaryChapter

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(diaryChapter: DiaryChapter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(diaryPage: DiaryPage)

    @Delete
    fun delete(diaryChapter: DiaryChapter)

    @Delete
    fun delete(diaryPage: DiaryPage)

    @Query("delete from diarychapter where name = :name")
    fun deleteChapterByName(name: String)

    @Query("select * from diarypage where chapter = :name")
    fun getPagesByChapterNameLive(name: String): LiveData<List<DiaryPage>>

    @Query("select * from diarypage where chapter = :name")
    fun getPagesByChapterName(name: String): List<DiaryPage>

    @Query("SELECT COUNT(id) FROM diarypage where chapter = :name")
    fun getNumberOfPages(name: String): LiveData<Int>

    @Transaction
    @Query("SELECT * from DiaryChapter")
    fun getCoverCards(): LiveData<List<CoverCard>>
}