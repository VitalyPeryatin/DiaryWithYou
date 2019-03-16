package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.view.*
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
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_cover.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class CoverRecyclerAdapter(lifecycleOwner: LifecycleOwner, val onItemClickListener: OnItemClickListener,
                           val onItemActionsClickListener: OnItemActionsClickListener):
    RecyclerView.Adapter<CoverRecyclerAdapter.CoverViewHolder>(){

    private var items = listOf<DiaryChapter>()

    private var oldFilteredItems = listOf<DiaryChapter>()
    private var filteredItems = MutableLiveData<List<DiaryChapter>>()
    private var viewWithActionBar: AppCompatActivity? = null
    private val diaryDao = App.instance.db.diaryDao()
    private var selectedChapter: DiaryChapter? = null

    init {
        diaryDao.getAllChapters().observe(lifecycleOwner, Observer<List<DiaryChapter>> { newItems ->
            items = newItems.reversed()
            filteredItems.postValue(items)
        })

        filteredItems.observe(lifecycleOwner, Observer<List<DiaryChapter>> { newItems ->
            val coverDiffUtilCallback = CoverDiffUtilCallback(oldFilteredItems, newItems)
            val diffResult = DiffUtil.calculateDiff(coverDiffUtilCallback)
            oldFilteredItems = newItems
            diffResult.dispatchUpdatesTo(this)
        })
    }

    fun getData(): List<DiaryChapter>{
        return oldFilteredItems
    }

    interface OnItemClickListener{
        fun onItemClick(text: String)
    }

    interface OnItemActionsClickListener{
        fun onShareClick()
        fun onInfoClick()
    }

    fun addChapter(diaryChapter: DiaryChapter){
        GlobalScope.launch(Dispatchers.IO) {
            diaryDao.insertChapter(diaryChapter)
        }
    }


    private var currentActionMode: ActionMode? = null
    private val modeCallback: ActionMode.Callback = object: ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.menu_remove -> {
                    GlobalScope.launch {
                        selectedChapter?.let {
                            diaryDao.delete(it)
                            File(it.pdfPath).delete()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    App.instance.baseContext,
                                "Глава \"${selectedChapter!!.name}\" удалена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            mode?.finish()
                        }
                    }
                }
                else -> return false
            }
            return true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.title = "Title"
            mode?.menuInflater?.inflate(R.menu.menu_action_chapters, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            currentActionMode = null
            selectedChapter = null
        }

    }

    fun filter(text: String){
        filteredItems.postValue(items.filter {
                s -> text.toLowerCase() in s.name.toLowerCase()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cover, parent, false)
        return CoverViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.value?.size ?: 0

    override fun onBindViewHolder(holder: CoverViewHolder, position: Int) {
        filteredItems.value?.let { items ->
            holder.tvName.text = items[position].name
            holder.ivInfo.setOnClickListener {
                Toast.makeText(App.instance.baseContext, "Информация о главе", Toast.LENGTH_SHORT).show()
            }
            holder.ivShare.setOnClickListener {
                Toast.makeText(App.instance.baseContext, "Поделиться с пользователями", Toast.LENGTH_SHORT).show()
            }
            val coverPath = items[position].coverPath
            if(coverPath == null){
                Picasso.get()
                    .load(R.drawable.default_cover1)
                    .placeholder(R.drawable.default_cover1)
                    .into(holder.ivCover)
            }else{
                Picasso.get()
                    .load(coverPath)
                    .placeholder(R.drawable.default_cover1)
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

    inner class CoverViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvName = view.tvName!!
        val ivShare = view.ivShare!!
        val ivInfo = view.ivInfo!!
        val ivCover = view.ivCover!!

        init {
            view.setOnClickListener {
                onItemClickListener.onItemClick(view.tvName.text.toString())
            }
            view.setOnLongClickListener {
                if (currentActionMode != null) { return@setOnLongClickListener false }
                    viewWithActionBar?.let {
                        selectedChapter = filteredItems.value!![layoutPosition]
                        it.startSupportActionMode(modeCallback)
                        view.isSelected = true
                    }
                        true
            }
            ivInfo.setOnClickListener {

            }
        }
    }
}