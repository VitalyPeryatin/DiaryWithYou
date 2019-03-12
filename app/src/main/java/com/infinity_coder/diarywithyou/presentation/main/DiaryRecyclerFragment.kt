package com.infinity_coder.diarywithyou.presentation.main

import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.fragment_diary_recycler.*
import java.io.FileOutputStream
import kotlin.random.Random

class DiaryRecyclerFragment: Fragment(), ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnItemClickListener, View.OnClickListener,
    Searchable {

    lateinit var adapter: CoverRecyclerAdapter
    lateinit var onItemClickListener: CoverRecyclerAdapter.OnItemClickListener
    lateinit var chapterNameDialog: ChapterNameDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CoverRecyclerAdapter(this)
        chapterNameDialog = ChapterNameDialog()

        recyclerCover.layoutManager = GridLayoutManager(context, 2)
        recyclerCover.adapter = adapter

        fabRecycler.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.addViewWithActionBar(activity as AppCompatActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.removeViewWithActionBar()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabRecycler -> {
                chapterNameDialog.show(childFragmentManager, "dialog")
//                val name = "Текст ${Random(100)}"
//                val diaryChapter = DiaryChapter(name, "${Environment.getExternalStorageDirectory().absoluteFile}/$name.pdf", null)
//                adapter.addChapter(diaryChapter)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showFab()
    }

    fun hideFab(){
        fabRecycler.hide()
    }

    fun showFab(){
        fabRecycler.show()
    }

    override fun addChapter(chapter: DiaryChapter) {
        adapter.addChapter(chapter)
        createPdf(chapter)
    }

    fun createPdf(chapter: DiaryChapter){
        val pathName = "${Environment.getExternalStorageDirectory().absoluteFile}/${chapter.name}.pdf"
        chapter.pdfPath = pathName
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pathName))
        document.open()
        document.add(Paragraph(" "))
        document.close()
    }

    companion object {
        fun newInstance(onItemClickListener: CoverRecyclerAdapter.OnItemClickListener): DiaryRecyclerFragment {
            val fragment = DiaryRecyclerFragment()
            fragment.onItemClickListener = onItemClickListener
            return fragment
        }
    }

    override fun search(text: String) {
        adapter.filter(text)
    }

    override fun onItemClick(text: String) {
        onItemClickListener.onItemClick(text)
    }
}