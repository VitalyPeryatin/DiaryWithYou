package com.infinity_coder.diarywithyou.data.repositories

import androidx.lifecycle.LiveData
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import com.infinity_coder.diarywithyou.domain.diary_pages.IDiaryPagesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DiaryPagesRepository: IDiaryPagesRepository {

    private val diaryDao = App.instance.db.diaryDao()

    override fun getPagesByChapterNameLive(chapterName: String?): LiveData<List<DiaryPage>>? {
        if(chapterName != null)
            return diaryDao.getPagesByChapterNameLive(chapterName)
        return null
    }

    override fun insertPage(diaryPage: DiaryPage) {
        GlobalScope.launch(Dispatchers.IO){
            diaryDao.insertPage(diaryPage)
        }
    }
}