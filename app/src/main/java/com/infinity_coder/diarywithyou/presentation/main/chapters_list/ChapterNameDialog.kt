package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.content.DialogInterface
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.ListPopupWindow.MATCH_PARENT
import androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import kotlinx.android.synthetic.main.dialog_chapter_name.view.*


class ChapterNameDialog: DialogFragment() {

    private val onCancelClickListener = View.OnClickListener {
        dismiss()
    }

    lateinit var etChapterName: EditText

    private val onOkClickListener = View.OnClickListener {
        val name = etChapterName.text.toString()
        if(name.isNotEmpty()) {
            val diaryChapter =
                DiaryChapter(name, "${Environment.getExternalStorageDirectory().absoluteFile}/$name.pdf", null)
            chapterListener.addChapter(diaryChapter)
            dismiss()
        }
    }

    interface OnChapterNameDialogListener{
        fun addChapter(chapter: DiaryChapter)
    }

    lateinit var chapterListener: OnChapterNameDialogListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_chapter_name, container, false)
        etChapterName = view.etChapterName
        view.btnOk.setOnClickListener(onOkClickListener)
        view.btnCancel.setOnClickListener(onCancelClickListener)
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        chapterListener = (parentFragment as OnChapterNameDialogListener)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        etChapterName.setText("")
    }
}
