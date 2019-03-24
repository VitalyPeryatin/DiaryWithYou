package com.infinity_coder.diarywithyou.domain.diary_pages

import androidx.lifecycle.LiveData
import com.infinity_coder.diarywithyou.data.db.DiaryPage

interface IDiaryPagesRepository {

    fun getPagesByChapterNameLive(chapterName: String?): LiveData<List<DiaryPage>>?
    fun insertPage(diaryPage: DiaryPage)
}