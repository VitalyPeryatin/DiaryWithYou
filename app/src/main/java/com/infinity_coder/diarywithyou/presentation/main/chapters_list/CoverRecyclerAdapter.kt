package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import kotlinx.android.synthetic.main.item_cover.view.*
import kotlinx.coroutines.*

class CoverRecyclerAdapter(
    private val lifecycleOwner: LifecycleOwner,
    val onItemClickListener: OnItemClickListener):
    RecyclerView.Adapter<CoverRecyclerAdapter.CoverViewHolder>(){

    private var items = listOf<DiaryChapter>()

    var oldFilteredItems = listOf<DiaryChapter>()
    var filteredItems = MutableLiveData<List<DiaryChapter>>()
    private set
    var viewWithActionBar: AppCompatActivity? = null
    val diaryDao = App.instance.db.diaryDao()
    var selectedChapter: DiaryChapter? = null

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

    interface OnItemClickListener{
        fun onItemClick(text: String)
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
                        diaryDao.delete(selectedChapter!!)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                App.instance.baseContext,
                                "Глава \"${selectedChapter!!.name}\" удалена",
                                Toast.LENGTH_SHORT
                            ).show()
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
        }
    }

    fun addViewWithActionBar(view: AppCompatActivity?){
        viewWithActionBar = view
    }

    fun removeViewWithActionBar(){
        viewWithActionBar = null
    }

    inner class CoverViewHolder(view: View): RecyclerView.ViewHolder(view) {
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
        }
        val tvName = view.tvName!!
        val ivShare = view.ivShare
        val ivInfo = view.ivInfo
    }
}