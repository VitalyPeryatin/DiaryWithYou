package com.infinity_coder.diarywithyou.domain.diary_pages

import androidx.lifecycle.LiveData
import com.infinity_coder.diarywithyou.data.db.DiaryPage

class DiaryPagesInteractor(private val diaryPagesRepository: IDiaryPagesRepository) {

    fun getPagesByChapterNameLive(chapterName: String?): LiveData<List<DiaryPage>>? {
        return diaryPagesRepository.getPagesByChapterNameLive(chapterName)
    }

    fun insertPage(diaryPage: DiaryPage){
        diaryPagesRepository.insertPage(diaryPage)
    }

    fun deletePage(diaryPage: DiaryPage){
        diaryPagesRepository.deletePage(diaryPage)
    }

    fun deleteAllPage(diaryPage: List<DiaryPage>){
        for(page in diaryPage)
            deletePage(page)
    }
}