package com.infinity_coder.diarywithyou.domain.chapter

import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.db.DiaryPage

interface IChapterRepository {
    fun insertChapter(diaryChapter: DiaryChapter)
    fun deleteChapter(diaryChapter: DiaryChapter)
    fun getPagesByChapterName(text: String): List<DiaryPage>
}