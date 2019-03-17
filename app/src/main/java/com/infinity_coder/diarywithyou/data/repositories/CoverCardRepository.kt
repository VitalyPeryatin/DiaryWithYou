package com.infinity_coder.diarywithyou.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.data.db.CoverCard
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class CoverCardRepository {
    val diaryDao = App.instance.db.diaryDao()
    val coverCardsLive = MutableLiveData<List<CoverCard>>()

    fun getChapters(): LiveData<List<DiaryChapter>>{
        return diaryDao.getAllChapters()
    }

    fun getCoverCards(): LiveData<List<CoverCard>> {
        return diaryDao.getCoverCards()
    }
}