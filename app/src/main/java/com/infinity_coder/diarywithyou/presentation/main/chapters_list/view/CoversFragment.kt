package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.data.repositories.chapters.ChapterRepository
import com.infinity_coder.diarywithyou.domain.chapter.ChapterInteractor
import com.infinity_coder.diarywithyou.presentation.DIALOG_FRAGMENT_KEY
import com.infinity_coder.diarywithyou.presentation.PDF_EXT
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler.CoverRecyclerAdapter
import com.infinity_coder.diarywithyou.presentation.toast
import com.infinity_coder.diarywithyou.utils.PdfCreator
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.fragment_cover_recycler.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CoversFragment: Fragment(),
    ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnChapterClickListener,
    CoverRecyclerAdapter.OnShareClickListener, View.OnClickListener, Searchable {

    private lateinit var adapter: CoverRecyclerAdapter
    private lateinit var onChapterClickListener: CoverRecyclerAdapter.OnChapterClickListener
    private lateinit var chapterNameDialog: ChapterNameDialog
    private val chapterInteractor = ChapterInteractor(ChapterRepository())
    private var isFloatPanelOpen = false

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && fabRecycler.isShown)
                fabRecycler.hide()
            else
                fabRecycler.show()
        }
    }

    private val dropDownUpListener = View.OnClickListener {
        if(isFloatPanelOpen) {
            btnDropDownUp.animate().rotationBy(180f).rotation(0f)
            frameSiteLink.visibility = GONE
        }
        else {
            btnDropDownUp.animate().rotation(180f)
            frameSiteLink.visibility = VISIBLE
        }
        isFloatPanelOpen = !isFloatPanelOpen
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(com.infinity_coder.diarywithyou.R.layout.fragment_cover_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter =
            CoverRecyclerAdapter(
                this,
                this,
                this
            )
        chapterNameDialog = ChapterNameDialog()

        recyclerCover.layoutManager = GridLayoutManager(context, 2)
        recyclerCover.adapter = adapter
        recyclerCover.addOnScrollListener(onScrollListener)

        fabRecycler.setOnClickListener(this)
        btnDropDownUp.setOnClickListener(dropDownUpListener)

        frameSiteLink.visibility = GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter.addViewWithActionBar(activity as AppCompatActivity)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).optionsMenu.postValue(MainActivity.MenuType.CHAPTERS)
        (activity as MainActivity).activeFragment = this
    }

    /**
     * Создаёт pdf документ из имеющихся страниц в главе и предоставляет возможность поделиться
     * pdf документом через другие приложения
     */
    override fun onShareClick(text: String) {
        val pages = chapterInteractor.getPagesByChapterName(text)
        val pdfPath = try {
            val rootDir = App.instance.getRootDir()
            if (rootDir == null)
                throw IOException()
            else
                PdfCreator.createPdf(pages, "$rootDir/$text.pdf")
        } catch (e: IOException) {
            null
        }
        if (pdfPath == null)
            context?.toast(resources.getString(R.string.empty_document))
        else {
            val intent = Intent(Intent.ACTION_SEND)
            val apkURI = FileProvider.getUriForFile(context!!,
                context?.applicationContext?.packageName + ".provider", File(pdfPath))
            intent.putExtra(Intent.EXTRA_STREAM, apkURI)
            intent.type = "application/pdf"
            try {
                startActivity(Intent.createChooser(intent, resources.getString(R.string.share_with)))
            } catch (e: ActivityNotFoundException) {
                context?.toast(resources.getString(R.string.no_app_to_open_pdf) + pdfPath)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabRecycler -> {
                chapterNameDialog.show(childFragmentManager, DIALOG_FRAGMENT_KEY)
                (activity as MainActivity).closeSearchView()
            }
        }
    }

    /**
     * Добавляет новую главу в список и в БД
     */
    override fun addChapter(chapter: DiaryChapter) {
        adapter.addChapter(chapter)
        if(adapter.getCovers().isNotEmpty()) {
            GlobalScope.launch {
                delay(800)
                recyclerCover.smoothScrollToPosition(0)
            }
        }
        createEmptyPdf(chapter)
    }

    private fun createEmptyPdf(chapter: DiaryChapter){
        val pathName = "${context!!.filesDir}/${chapter.name}.$PDF_EXT"
        chapter.pdfPath = pathName
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pathName))
        document.open()
        document.add(Paragraph(" "))
        document.close()
    }

    /**
     * Фильтрует список по введённому пользователем названием главы
     */
    override fun searchByDate(text: String) {
        adapter.filter(text)
    }

    /**
     * Открывает фрагмент со страницами выбранной главы.
     */
    override fun onChapterClick(text: String) {
        onChapterClickListener.onChapterClick(text)
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).activeFragment = null
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.removeViewWithActionBar()
    }

    companion object {
        fun newInstance(onChapterClickListener: CoverRecyclerAdapter.OnChapterClickListener): CoversFragment {
            val fragment = CoversFragment()
            fragment.onChapterClickListener = onChapterClickListener
            return fragment
        }
    }
}