package com.infinity_coder.diarywithyou.domain.chapter

import com.infinity_coder.diarywithyou.data.db.DiaryChapter

class ChapterInteractor(private val chapterRepository: IChapterRepository) {
    fun insertChapter(diaryChapter: DiaryChapter) {
        chapterRepository.insertChapter(diaryChapter)
    }

    fun deleteChapter(diaryChapter: DiaryChapter) {
        chapterRepository.deleteChapter(diaryChapter)
    }
}