package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.ListPopupWindow.MATCH_PARENT
import androidx.appcompat.widget.ListPopupWindow.WRAP_CONTENT
import androidx.fragment.app.DialogFragment
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.RESULT_LOAD_IMAGE
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_chapter_name.*
import kotlinx.android.synthetic.main.dialog_chapter_name.view.*
import java.io.File


class ChapterNameDialog: DialogFragment() {

    private val onCancelClickListener = View.OnClickListener {
        dismiss()
    }

    lateinit var etChapterName: EditText
    private var coverPath: String? = null

    private val onOkClickListener = View.OnClickListener {
        val name = etChapterName.text.toString()
        if(name.isNotEmpty()) {
            val diaryChapter =
                DiaryChapter(name, "${context!!.filesDir}/$name.pdf", coverPath)
            chapterListener.addChapter(diaryChapter)
            dismiss()
        }
    }

    private val onCoverClickListener = View.OnClickListener {
        val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RESULT_LOAD_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            inflateImage(data.data)
        }
    }

    private fun inflateImage(selectedImage: Uri){
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity!!.contentResolver.query(selectedImage, filePathColumn, null, null, null)
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val picturePath = cursor.getString(columnIndex)
        cursor.close()
        Picasso.get()
            .load(File(picturePath))
            .placeholder(R.drawable.default_cover1)
            .transform(CropSquareTransformation())
            .into(ivCover)
        coverPath = picturePath
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
        view.ivCover.setOnClickListener(onCoverClickListener)
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
