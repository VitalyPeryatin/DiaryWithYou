package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler

import androidx.recyclerview.widget.DiffUtil
import com.infinity_coder.diarywithyou.data.db.CoverCard

class CoverDiffUtilCallback(private val oldList: List<CoverCard>,
                            private val newList: List<CoverCard>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].diaryChapter!!.id == newList[newItemPosition].diaryChapter!!.id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].diaryChapter!!.name == newList[newItemPosition].diaryChapter!!.name &&
            oldList[oldItemPosition].diaryChapter!!.pdfPath == newList[newItemPosition].diaryChapter!!.pdfPath &&
            oldList[oldItemPosition].diaryChapter!!.coverPath == newList[newItemPosition].diaryChapter!!.coverPath
}
