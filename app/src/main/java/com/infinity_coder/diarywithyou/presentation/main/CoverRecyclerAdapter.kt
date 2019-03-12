package com.infinity_coder.diarywithyou.presentation.main

import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import kotlinx.android.synthetic.main.item_cover.view.*
import kotlinx.coroutines.*

class CoverRecyclerAdapter(val onItemClickListener: OnItemClickListener):
    RecyclerView.Adapter<CoverRecyclerAdapter.CoverViewHolder>(){

    var items = mutableListOf<DiaryChapter>()
    private var filterItems = mutableListOf<DiaryChapter>()
    var viewWithActionBar: AppCompatActivity? = null
    val diaryDao = App.instance.db.diaryDao()
    init {
        GlobalScope.launch(Dispatchers.IO) {
            items = diaryDao.getAll() as MutableList<DiaryChapter>
            filterItems.addAll(items)
            withContext(Dispatchers.Main){
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(text: String)
    }

    fun addChapter(diaryChapter: DiaryChapter){
        GlobalScope.launch(Dispatchers.IO) {
            diaryDao.insert(diaryChapter)
            items.add(diaryChapter)
            filterItems.add(diaryChapter)
            withContext(Dispatchers.Main){
                this@CoverRecyclerAdapter.notifyDataSetChanged()
            }
        }
    }


    private var currentActionMode: ActionMode? = null
    private val modeCallback: ActionMode.Callback = object: ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId){
                R.id.menu_remove -> {
                    Toast.makeText(App.instance.baseContext, "ОК", Toast.LENGTH_SHORT).show()
                    mode?.finish()
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
        }

    }

    fun filter(text: String){
        filterItems.clear()
        for(item in items){
            if(text in item.name){
                filterItems.add(item)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cover, parent, false)
        return CoverViewHolder(view)
    }

    override fun getItemCount(): Int = filterItems.size

    override fun onBindViewHolder(holder: CoverViewHolder, position: Int) {
        holder.tvName.text = filterItems[position].name
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
                        it.startSupportActionMode(modeCallback)
                        view.isSelected = true
                    }
                        true
            }
        }
        val tvName = view.tvName!!
    }
}