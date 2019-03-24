package com.infinity_coder.diarywithyou.presentation.main.chapter_pages.view.recycler

import androidx.recyclerview.widget.DiffUtil
import com.infinity_coder.diarywithyou.data.db.DiaryPage

/**
 * Сравнивает старый список, переданный в адаптер, и новый. Если элементы разные, элементы обновляются.
 */
class PageDiffUtilCallback(private val oldList: List<DiaryPage>,
                           private val newList: List<DiaryPage>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].chapter == newList[newItemPosition].chapter &&
            oldList[oldItemPosition].imagePath == newList[newItemPosition].imagePath &&
            oldList[oldItemPosition].date == newList[newItemPosition].date
}