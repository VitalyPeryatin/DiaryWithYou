package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view

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
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.COVER_NAME_KEY
import com.infinity_coder.diarywithyou.presentation.MODE_KEY
import com.infinity_coder.diarywithyou.presentation.RESULT_LOAD_IMAGE
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler.CoverRecyclerAdapter
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view_model.CropSquareTransformation
import com.infinity_coder.diarywithyou.presentation.toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_chapter_name.*
import kotlinx.android.synthetic.main.dialog_chapter_name.view.*
import java.io.File


class ChapterNameDialog: DialogFragment() {

    enum class Mode {CREATE, EDIT}
    private lateinit var etChapterName: EditText
    private var coverPath: String? = null
    private lateinit var chapterListener: OnChapterNameDialogListener

    private var mode = Mode.CREATE
    private var editChapterName: String? = null
    private lateinit var editChapter: DiaryChapter


    /**
     * Слушатель по нажатию на кнопку "Отимена", закрывает диалоговое окно
     */
    private val onCancelClickListener = View.OnClickListener {
        dismiss()
    }

    /**
     * Слушатель по нажатию на кнопку "Ок". Создаёт новую главу, добавляет её в БД и создаёт новый pdf документ с
     * соответствующим названием.
     */
    private val onOkClickListener = View.OnClickListener {
        val name = etChapterName.text.toString()
        if(name.isNotEmpty()) {
            if(mode == Mode.CREATE) {
                val diaryChapter =
                    DiaryChapter(name, "${context!!.filesDir}/$name.pdf", coverPath)
                chapterListener.addChapter(diaryChapter)
                dismiss()
            }
            else if(mode == Mode.EDIT && editChapterName != null){
                val file = File("${context!!.filesDir}/${editChapter.name}.pdf")
                file.renameTo(File("${context!!.filesDir}/$name.pdf"))
                editChapter.name = name
                if(coverPath != null && coverPath != editChapter.coverPath)
                    editChapter.coverPath = coverPath
                chapterListener.updateChapter(editChapter)
                dismiss()
            }
        }
    }

    /**
     * Слушатель по нажатию на иконку выбора изображения.
     * Откоывает галерею для выбора обложки и возвращает её в качестве результата.
     */
    private val onCoverClickListener = View.OnClickListener {
        val i = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(i, RESULT_LOAD_IMAGE)
    }

    /**
     * Слушатель добавления нового заголовка из диалогового окна
     */
    interface OnChapterNameDialogListener{
        fun addChapter(chapter: DiaryChapter)
        fun updateChapter(chapter: DiaryChapter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_chapter_name, container, false)
        etChapterName = view.etChapterName

        val modeName = arguments!!.getString(MODE_KEY, Mode.CREATE.name)
        mode = Mode.valueOf(modeName)

        view.btnOk.setOnClickListener(onOkClickListener)
        view.btnCancel.setOnClickListener(onCancelClickListener)
        view.ivCover.setOnClickListener(onCoverClickListener)
        return view
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(mode == Mode.EDIT) {
            editChapterName = editChapter.name
            if(editChapterName != null)
                view.etChapterName.setText(editChapterName)
            if(editChapter.coverPath != null && File(editChapter.coverPath).exists()) {
                Picasso.get().load(File(editChapter.coverPath))
                    .transform(CropSquareTransformation())
                    .placeholder(R.drawable.image_placeholder)
                    .fit()
                    .noFade()
                    .centerCrop()
                    .noFade()
                    .into(view.ivCover)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        etChapterName.setText("")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            inflateImage(data.data!!)
        }
    }

    /**
     * Достёт изображение из галереи и устанавливает её в качестве обложки
     */
    private fun inflateImage(selectedImage: Uri){
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity!!.contentResolver.query(selectedImage, filePathColumn, null, null, null)
        cursor?.apply {
            moveToFirst()
            val columnIndex = getColumnIndex(filePathColumn[0])
            val picturePath = getString(columnIndex)
            close()
            Picasso.get()
                .load(File(picturePath))
                .placeholder(R.drawable.default_cover1)
                .transform(CropSquareTransformation())
                .into(ivCover)
            coverPath = picturePath
        }
    }

    companion object {
        fun newInstance(chapterClickListener: ChapterNameDialog.OnChapterNameDialogListener): ChapterNameDialog{
            val fragment = ChapterNameDialog()
            val bundle = Bundle()
            bundle.putString(MODE_KEY, Mode.CREATE.toString())
            fragment.arguments = bundle
            fragment.chapterListener = chapterClickListener
            return fragment
        }

        fun newInstance(chapterClickListener: ChapterNameDialog.OnChapterNameDialogListener, chapter: DiaryChapter): ChapterNameDialog{
            val fragment = ChapterNameDialog()
            fragment.editChapter = chapter
            val bundle = Bundle()
            bundle.putString(MODE_KEY, Mode.EDIT.toString())
            fragment.arguments = bundle
            fragment.chapterListener = chapterClickListener
            return fragment
        }
    }
}
