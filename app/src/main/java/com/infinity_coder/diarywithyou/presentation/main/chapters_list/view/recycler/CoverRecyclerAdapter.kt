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
import com.infinity_coder.diarywithyou.data.repositories.ChapterRepository
import com.infinity_coder.diarywithyou.domain.chapter.ChapterInteractor
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view_model.CropSquareTransformation
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_cover.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class CoverRecyclerAdapter(lifecycleOwner: LifecycleOwner, val onItemClickListener: OnItemClickListener,
    val onShareClickListener: OnShareClickListener):
    RecyclerView.Adapter<CoverRecyclerAdapter.CoverViewHolder>(){

    private var items = listOf<CoverCard>()

    private var oldFilteredItems = listOf<CoverCard>()
    private var filteredItems = MutableLiveData<List<CoverCard>>()
    private var viewWithActionBar: AppCompatActivity? = null
    private val diaryDao = App.instance.db.diaryDao()
    private val res = App.instance.resources
    private val selectedChapters = mutableListOf<CoverCard>()

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

    fun getData(): List<CoverCard>{
        return oldFilteredItems
    }

    interface OnItemClickListener{
        fun onItemClick(text: String)
    }

    interface OnShareClickListener{
        fun onShareClick(text: String)
    }

    fun addChapter(diaryChapter: DiaryChapter){
        chapterInteractor.insertChapter(diaryChapter)
    }

    private var currentActionMode: ActionMode? = null
    private val modeCallback: ActionMode.Callback = object: ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.menu_remove -> {
                    GlobalScope.launch {
                        for(selectedChapter in selectedChapters) {
                            diaryDao.delete(selectedChapter.diaryChapter!!)
                            File(selectedChapter.diaryChapter!!.pdfPath).delete()
                            toast(
                                "${res.getString(R.string.chapter)} \"${selectedChapter.diaryChapter!!.name}\" ${res.getString(
                                    R.string.deleted
                                )}"
                            )
                        }
                        mode?.finish()
                    }
                }
                else -> return false
            }
            return true
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
            selectedChapters.clear()
        }
    }

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
            if(coverPath == null){
                Picasso.get()
                    .load(R.drawable.default_cover1)
                    .transform(CropSquareTransformation())
                    .placeholder(R.drawable.image_placeholder)
                    .fit()
                    .noFade()
                    .centerCrop()
                    .into(holder.ivCover)
            }else{
                Picasso.get()
                    .load(File(coverPath))
                    .transform(CropSquareTransformation())
                    .placeholder(R.drawable.image_placeholder)
                    .fit()
                    .noFade()
                    .centerCrop()
                    .into(holder.ivCover)
            }
        }
    }

    fun addViewWithActionBar(view: AppCompatActivity?){
        viewWithActionBar = view
    }

    fun removeViewWithActionBar(){
        viewWithActionBar = null
    }

    private suspend fun toast(text: String){
        withContext(Dispatchers.Main) {
            Toast.makeText(App.instance.baseContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    inner class CoverViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvName = view.tvName!!
        val tvPageNum = view.tvPageNum!!
        val ivCover = view.ivCover!!
        private val ivMaskSelected = view.ivMaskSelected!!
        private val ivMaskSelectedDone = view.ivMaskSelectedDone!!

        init {
            view.ivShare!!.setOnClickListener {
                onShareClickListener.onShareClick(view.tvName.text.toString())
            }
            view.setOnClickListener {
                if(currentActionMode == null)
                    onItemClickListener.onItemClick(view.tvName.text.toString())
                else
                    actionItemSelect(view)
            }
            view.setOnLongClickListener {
                if (currentActionMode != null) { return@setOnLongClickListener false }
                    viewWithActionBar?.let {
                        actionItemSelect(view)
                        it.startSupportActionMode(modeCallback)
                    }
                        true
            }
        }

        private fun actionItemSelect(view: View){
            viewWithActionBar?.let {
                if(!view.isSelected) {
                    selectedChapters.add(filteredItems.value!![layoutPosition])
                    ivMaskSelected.visibility = VISIBLE
                    ivMaskSelectedDone.visibility = VISIBLE
                }
                else {
                    selectedChapters.remove(filteredItems.value!![layoutPosition])
                    ivMaskSelected.visibility = GONE
                    ivMaskSelectedDone.visibility = GONE
                }
                currentActionMode?.title = selectedChapters.size.toString()
                view.isSelected = !view.isSelected
            }
        }
    }
}