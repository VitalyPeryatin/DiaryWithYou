package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_diary_recycler.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class DiaryRecyclerFragment: Fragment(),
    ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnItemClickListener, CoverRecyclerAdapter.OnItemActionsClickListener,
    View.OnClickListener, Searchable {

    lateinit var adapter: CoverRecyclerAdapter
    lateinit var onItemClickListener: CoverRecyclerAdapter.OnItemClickListener
    lateinit var chapterNameDialog: ChapterNameDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CoverRecyclerAdapter(this, this, this)
        chapterNameDialog = ChapterNameDialog()

        recyclerCover.layoutManager = GridLayoutManager(context, 2)
        recyclerCover.adapter = adapter

        fabRecycler.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.addViewWithActionBar(activity as AppCompatActivity)
        (activity as MainActivity).activeFragment = this
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.removeViewWithActionBar()
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).optionsMenu.postValue(MainActivity.MenuType.CHAPTERS)
        (activity as MainActivity).activeFragment = this
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).activeFragment = null
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabRecycler -> {
                chapterNameDialog.show(childFragmentManager, "dialog")
                (activity as MainActivity).closeSearchView()
            }
        }
    }

    override fun addChapter(chapter: DiaryChapter) {
        adapter.addChapter(chapter)
        if(adapter.getData().isNotEmpty()) {
            GlobalScope.launch {
                delay(800)
                recyclerCover.smoothScrollToPosition(0)
            }
        }
        createPdf(chapter)
    }

    fun createPdf(chapter: DiaryChapter){
        val pathName = "${context!!.filesDir}/${chapter.name}.pdf"
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

    override fun onShareClick() {
    }

    override fun onInfoClick() {
    }
}