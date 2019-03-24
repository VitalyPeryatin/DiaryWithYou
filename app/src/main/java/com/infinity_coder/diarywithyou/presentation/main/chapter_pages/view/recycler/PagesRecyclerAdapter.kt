package com.infinity_coder.diarywithyou.presentation.main.chapter_pages.view.recycler

import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import com.infinity_coder.diarywithyou.data.repositories.pages.DiaryPagesRepository
import com.infinity_coder.diarywithyou.domain.diary_pages.DiaryPagesInteractor
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_page.view.*
import java.io.File

class PagesRecyclerAdapter(lifecycleOwner: LifecycleOwner, chapterName: String?):
    RecyclerView.Adapter<PagesRecyclerAdapter.PagesViewHolder>() {

    // Все страницы, лежащие в ежедневнике
    private var pages = listOf<DiaryPage>()
    // Старый отфильтрованный список
    private var oldFilteredPages = listOf<DiaryPage>()
    // Список, подписанный на изменения данных
    private var filteredPagesLive = MutableLiveData<List<DiaryPage>>()

    private var viewWithActionBar: AppCompatActivity? = null

    private val selectedPages = mutableListOf<DiaryPage>()

    private val diaryPagesInteractor =
        DiaryPagesInteractor(DiaryPagesRepository())

    init {
        diaryPagesInteractor.getPagesByChapterNameLive(chapterName)?.observe(lifecycleOwner, Observer<List<DiaryPage>> {
            pages = it
            filteredPagesLive.postValue(it)
        })

        // Подписывается на изменения и обновляет старый отфильтрованный список
        filteredPagesLive.observe(lifecycleOwner, Observer<List<DiaryPage>> {
            val coverDiffUtilCallback = PageDiffUtilCallback(oldFilteredPages, it)
            val diffResult = DiffUtil.calculateDiff(coverDiffUtilCallback)
            oldFilteredPages = it
            diffResult.dispatchUpdatesTo(this)
        })
    }

    private var currentActionMode: ActionMode? = null
    private val modeCallback: ActionMode.Callback = object: ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when(item?.itemId){
                R.id.menu_remove -> {
                    diaryPagesInteractor.deleteAllPage(selectedPages)
                    mode?.finish()
                    true
                }
                else -> return false
            }
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            currentActionMode = mode
            mode?.title = "1"
            mode?.menuInflater?.inflate(R.menu.menu_action_chapters, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            currentActionMode = null
            selectedPages.clear()
        }
    }

    /**
     * Получает все старницы ежедневника в отфильрованном виде из LiveData
     * @return список старниц ежедневника
     */
    fun getPages(): List<DiaryPage> = filteredPagesLive.value!!

    /**
     * Добавить страницу
     */
    fun addPage(chapterName: String, imagePath: String, date: String){
        diaryPagesInteractor.insertPage(DiaryPage(chapterName, imagePath, date))
    }

    /**
     * Фильтрует страницы по введённой дате
     */
    fun filter(text: String){
        filteredPagesLive.postValue(pages.filter {
                s -> text in s.date
        })
    }

    fun addViewWithActionBar(view: AppCompatActivity?){
        viewWithActionBar = view
    }

    fun removeViewWithActionBar(){
        viewWithActionBar = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false)
        return PagesViewHolder(
            view
        )
    }

    override fun getItemCount(): Int = oldFilteredPages.size

    override fun onBindViewHolder(holder: PagesViewHolder, position: Int) {
        Picasso.get()
            .load(File(oldFilteredPages[position].imagePath))
            .noFade()
            .into(holder.photo)
        holder.tvDate.text = oldFilteredPages[position].date
    }

    inner class PagesViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val photo = view.photo!!
        val tvDate = view.tvDate!!
        private val frameMask = view.frameMask!!
        private val ivMaskDone = view.ivMaskDone!!

        init {
            view.setOnLongClickListener {
                if (currentActionMode != null) { return@setOnLongClickListener false }
                viewWithActionBar?.let {
                    actionItemSelect(view)
                    it.startSupportActionMode(modeCallback)
                }
                true
            }

            view.setOnClickListener {
                if(currentActionMode != null)
                    actionItemSelect(view)
            }
        }

        private fun actionItemSelect(view: View){
            viewWithActionBar?.let {
                if(!view.isSelected) {
                    selectedPages.add(filteredPagesLive.value!![layoutPosition])
                    frameMask.visibility = View.VISIBLE
                    ivMaskDone.visibility = View.VISIBLE
                }
                else {
                    selectedPages.remove(filteredPagesLive.value!![layoutPosition])
                    frameMask.visibility = View.GONE
                    ivMaskDone.visibility = View.GONE
                }
                currentActionMode?.title = selectedPages.size.toString()
                view.isSelected = !view.isSelected
            }
        }
    }
}