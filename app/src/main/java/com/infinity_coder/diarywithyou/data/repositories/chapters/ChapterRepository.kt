package com.infinity_coder.diarywithyou.data.repositories.chapters

import androidx.lifecycle.LiveData
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.data.db.CoverCard
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.domain.chapter.IChapterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChapterRepository: IChapterRepository {

    private val diaryDao = App.instance.db.diaryDao()

    fun getCoverCards(): LiveData<List<CoverCard>> {
        return diaryDao.getCoverCards()
    }

    override fun insertChapter(diaryChapter: DiaryChapter) {
        GlobalScope.launch(Dispatchers.IO) {
            diaryDao.insert(diaryChapter)
        }
    }

    override fun deleteChapter(diaryChapter: DiaryChapter) {
        GlobalScope.launch(Dispatchers.IO) {
            diaryDao.delete(diaryChapter)
        }
    }
}