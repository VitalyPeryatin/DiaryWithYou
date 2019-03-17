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

    @Query("SELECT COUNT(id) FROM diarypage where chapter = :name")
    fun getNumberOfPages(name: String): LiveData<Int>

    @Transaction
    @Query("SELECT * from DiaryChapter")
    fun getCoverCards(): LiveData<List<CoverCard>>
}