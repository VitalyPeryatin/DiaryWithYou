package com.infinity_coder.diarywithyou.presentation.main.chapter_pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_page.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class PagesRecyclerAdapter(private val lifecycleOwner: LifecycleOwner,
                           val chapterName: String?): RecyclerView.Adapter<PagesRecyclerAdapter.PagesViewHolder>() {


    private var pages = listOf<DiaryPage>()
    private var oldFilteredPages = listOf<DiaryPage>()
    private var filteredPagesLive = MutableLiveData<List<DiaryPage>>()
    val diaryDao = App.instance.db.diaryDao()

    init {
        diaryDao.getPagesByChapterName(chapterName!!).observe(lifecycleOwner, Observer<List<DiaryPage>> {
            pages = it
            filteredPagesLive.postValue(it)
        })

        filteredPagesLive.observe(lifecycleOwner, Observer<List<DiaryPage>> {
            val coverDiffUtilCallback = PageDiffUtilCallback(oldFilteredPages, it)
            val diffResult = DiffUtil.calculateDiff(coverDiffUtilCallback)
            oldFilteredPages = it
            diffResult.dispatchUpdatesTo(this)

        })
    }

    fun getPages(): List<DiaryPage>{
        return filteredPagesLive.value!!
    }

    fun addPage(chapterName: String, imagePath: String, date: String){
        GlobalScope.launch(Dispatchers.IO) {
            diaryDao.insertPage(DiaryPage(chapterName, imagePath, date))
        }
    }

    fun filter(text: String){
        filteredPagesLive.postValue(pages.filter {
                s -> text in s.date
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PagesViewHolder(view)
    }

    override fun getItemCount(): Int = oldFilteredPages.size

    override fun onBindViewHolder(holder: PagesViewHolder, position: Int) {
        Picasso.get()
            .load(File(oldFilteredPages[position].imagePath))
            .into(holder.photo)
        holder.tvDate.text = oldFilteredPages[position].date
    }

    class PagesViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val photo = view.photo!!
        val tvDate = view.tvDate!!
    }
}