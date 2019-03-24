package com.infinity_coder.diarywithyou.domain.chapter

import com.infinity_coder.diarywithyou.data.db.DiaryChapter

interface IChapterRepository {
    fun insertChapter(diaryChapter: DiaryChapter)
}