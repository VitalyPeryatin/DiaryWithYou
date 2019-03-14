package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import androidx.recyclerview.widget.DiffUtil
import com.infinity_coder.diarywithyou.domain.DiaryChapter

class CoverDiffUtilCallback(private val oldList: List<DiaryChapter>,
                            private val newList: List<DiaryChapter>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].name == newList[newItemPosition].name

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].pdfPath == newList[newItemPosition].pdfPath &&
            oldList[oldItemPosition].coverPath == newList[newItemPosition].coverPath
}