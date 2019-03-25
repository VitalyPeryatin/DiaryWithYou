package com.infinity_coder.diarywithyou.domain.chapter

import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.db.DiaryPage

class ChapterInteractor(private val chapterRepository: IChapterRepository) {
    fun insertChapter(diaryChapter: DiaryChapter) {
        chapterRepository.insertChapter(diaryChapter)
    }

    fun deleteChapter(diaryChapter: DiaryChapter) {
        chapterRepository.deleteChapter(diaryChapter)
    }

    fun getPagesByChapterName(text: String): List<DiaryPage>{
        return chapterRepository.getPagesByChapterName(text)
    }
}