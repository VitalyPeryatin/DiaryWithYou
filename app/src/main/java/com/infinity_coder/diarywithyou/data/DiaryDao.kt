package com.infinity_coder.diarywithyou.data

import androidx.room.*
import com.infinity_coder.diarywithyou.domain.DiaryChapter

@Dao
interface DiaryDao {
    @Query("select * from diarychapter")
    fun getAll(): List<DiaryChapter>

    @Query("select * from diarychapter where name = :name")
    fun getByName(name: String): DiaryChapter

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(diaryChapter: DiaryChapter)

    @Delete
    fun delete(diaryChapter: DiaryChapter)

    @Query("delete from diarychapter where name = :name")
    fun deleteByName(name: String)
}