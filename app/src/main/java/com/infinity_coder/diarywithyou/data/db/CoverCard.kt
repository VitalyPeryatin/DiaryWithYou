package com.infinity_coder.diarywithyou.data.db

import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation


class CoverCard {
    @Embedded
    var diaryChapter: DiaryChapter? = null
    @Relation(parentColumn = "name", entityColumn = "chapter", entity = DiaryPage::class)
    var pages: List<DiaryPage>? = null
    fun getPagesSize() = pages!!.size
}