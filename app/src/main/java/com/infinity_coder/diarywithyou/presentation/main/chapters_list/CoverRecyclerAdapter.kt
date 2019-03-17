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
import com.infinity_coder.diarywithyou.data.db.CoverCard
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.repositories.CoverCardRepository
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
    private var selectedChapter: CoverCard? = null

    init {
        val repository = CoverCardRepository()
        repository.getCoverCards().observe(lifecycleOwner, Observer<List<CoverCard>> {newItems ->
            items = newItems.reversed()
            filteredItems.postValue(items)
        })

        filteredItems.observe(lifecycleOwner, Observer<List<CoverCard>> { newItems ->
            val coverDiffUtilCallback = CoverDiffUtilCallback(oldFilteredItems, newItems)
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
                            diaryDao.delete(it.diaryChapter!!)
                            File(it.diaryChapter!!.pdfPath).delete()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    App.instance.baseContext,
                                "Глава \"${selectedChapter?.diaryChapter!!.name}\" удалена",
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

    inner class CoverViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvName = view.tvName!!
        val tvPageNum = view.tvPageNum!!
        val ivShare = view.ivShare!!
        val ivCover = view.ivCover!!

        init {
            ivShare.setOnClickListener(View.OnClickListener {
                onShareClickListener.onShareClick(view.tvName.text.toString())
            })
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
    }
}