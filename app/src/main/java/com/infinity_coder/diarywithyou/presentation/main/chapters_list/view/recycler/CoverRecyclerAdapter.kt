package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler

import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.CoverCard
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.repositories.chapters.ChapterRepository
import com.infinity_coder.diarywithyou.domain.chapter.ChapterInteractor
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view_model.CropSquareTransformation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_cover.view.*
import java.io.File


class CoverRecyclerAdapter(lifecycleOwner: LifecycleOwner, val onChapterClickListener: OnChapterClickListener,
                           val onShareClickListener: OnShareClickListener):
    RecyclerView.Adapter<CoverRecyclerAdapter.CoverViewHolder>(){

    private var items = listOf<CoverCard>()
    private var oldFilteredItems = listOf<CoverCard>()
    private var filteredItems = MutableLiveData<List<CoverCard>>()
    private var viewWithActionBar: AppCompatActivity? = null
    private val res = App.instance.resources
    private val selectedChapters = mutableListOf<CoverCard>()
    private val selectedViews = mutableListOf<CoverViewHolder>()
    private val chapterInteractor = ChapterInteractor(ChapterRepository())

    init {
        val repository = ChapterRepository()
        repository.getCoverCards().observe(lifecycleOwner, Observer<List<CoverCard>> {newItems ->
            items = newItems.reversed()
            filteredItems.postValue(items)
        })
        filteredItems.observe(lifecycleOwner, Observer<List<CoverCard>> { newItems ->
            val coverDiffUtilCallback =
                CoverDiffUtilCallback(
                    oldFilteredItems,
                    newItems
                )
            val diffResult = DiffUtil.calculateDiff(coverDiffUtilCallback)
            oldFilteredItems = newItems
            diffResult.dispatchUpdatesTo(this)
        })
    }

    /**
     * Интерфейс для прослушивания нажатий на обложки ежедневников
     */
    interface OnChapterClickListener{ fun onChapterClick(text: String) }

    /**
     * Интерфейс для прослушивания нажатий на кнопку "Поделиться"
     */
    interface OnShareClickListener{ fun onShareClick(text: String) }

    /**
     * Получает список обложек
     */
    fun getCovers(): List<CoverCard>{
        return oldFilteredItems
    }

    /**
     * Добавляет главу ежедневника в БД и список
     */
    fun addChapter(diaryChapter: DiaryChapter){
        chapterInteractor.insertChapter(diaryChapter)
    }

    /**
     * Работа с контекстным меню действий, здесь реализуется удаление элементов
     */
    private var currentActionMode: ActionMode? = null
    private val modeCallback: ActionMode.Callback = object: ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.menu_remove -> {
                    for(selectedChapter in selectedChapters) {
                        chapterInteractor.deleteChapter(selectedChapter.diaryChapter!!)
                        File(selectedChapter.diaryChapter!!.pdfPath).delete()
                        Toast.makeText(App.instance.baseContext,
                            "${res.getString(R.string.chapter)} \"${selectedChapter.diaryChapter!!.name}\" ${res.getString(R.string.deleted)}",
                            Toast.LENGTH_SHORT).show()
                    }
                    mode?.finish()
                }
                else -> return false
            }
            return true
        }

        /**
         * Инициализирует контекстное меню и присваивает заголовку "1" - кол-во выбранных элементов
         */
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            currentActionMode = mode
            mode?.title = "1"
            mode?.menuInflater?.inflate(R.menu.menu_action_chapters, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        /**
         * Убирает контестное меню и очищает выбранные элементы
         */
        override fun onDestroyActionMode(mode: ActionMode?) {
            currentActionMode = null
            selectedChapters.clear()
            for(view in selectedViews) {
                view.ivMaskSelected.visibility = GONE
                view.ivMaskSelectedDone.visibility = GONE
            }
            selectedViews.clear()
        }
    }

    /**
     * Фильтрует элементы по названию глав ежедневника
     */
    fun filter(text: String){
        filteredItems.postValue(items.filter {
                s -> text.toLowerCase() in s.diaryChapter!!.name.toLowerCase()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cover, parent, false)
        return CoverViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.value?.size ?: 0

    override fun onBindViewHolder(holder: CoverViewHolder, position: Int) {
        filteredItems.value?.let { items ->
            holder.tvName.text = items[position].diaryChapter!!.name
            holder.tvPageNum.text = items[position].getPagesSize().toString()
            val coverPath = items[position].diaryChapter!!.coverPath

            val picasso = if(coverPath == null)
                Picasso.get().load(R.drawable.default_cover1)
            else
                Picasso.get().load(File(coverPath))

            picasso.transform(CropSquareTransformation())
                .placeholder(R.drawable.image_placeholder)
                .fit()
                .noFade()
                .centerCrop()
                .into(holder.ivCover)
        }
    }

    /**
     * Утсанавливает Activity, у которого есть ActionBar для открытия контекстного меню действий
     */
    fun addViewWithActionBar(view: AppCompatActivity?){
        if(view != null && view.supportActionBar != null)
            viewWithActionBar = view
    }

    /**
     * Удаляет Activity, у которого есть ActionBar для открытия контекстного меню действий
     */
    fun removeViewWithActionBar(){
        viewWithActionBar = null
    }

    inner class CoverViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvName = view.tvName!!
        val tvPageNum = view.tvPageNum!!
        val ivCover = view.ivCover!!
        val ivMaskSelected = view.ivMaskSelected!!
        val ivMaskSelectedDone = view.ivMaskSelectedDone!!

        init {
            view.ivShare!!.setOnClickListener {
                onShareClickListener.onShareClick(view.tvName.text.toString())
            }
            view.setOnClickListener {
                if(currentActionMode == null)
                    onChapterClickListener.onChapterClick(view.tvName.text.toString())
                else
                    actionItemSelect(view)
            }
            view.setOnLongClickListener {
                if (currentActionMode != null) { return@setOnLongClickListener false }
                    viewWithActionBar?.let { activity ->
                        actionItemSelect(view)
                        activity.startSupportActionMode(modeCallback)
                    }
                        true
            }
        }

        /**
         * Добавляет выбранный элемент в список выбранных элементов и
         * графически показывает, что элемент выбран
         * @param view элемент, который выбрали
         */
        private fun actionItemSelect(view: View){
            viewWithActionBar?.let {
                if(!view.isSelected) {
                    selectedChapters.add(filteredItems.value!![layoutPosition])
                    selectedViews.add(this)
                    ivMaskSelected.visibility = VISIBLE
                    ivMaskSelectedDone.visibility = VISIBLE
                }
                else {
                    selectedChapters.remove(filteredItems.value!![layoutPosition])
                    selectedViews.remove(this)
                    ivMaskSelected.visibility = GONE
                    ivMaskSelectedDone.visibility = GONE
                }
                currentActionMode?.title = selectedChapters.size.toString()
                view.isSelected = !view.isSelected
            }
        }
    }
}