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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.DIALOG_FRAGMENT_KEY
import com.infinity_coder.diarywithyou.presentation.PDF_EXT
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.infinity_coder.diarywithyou.presentation.toast
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.fragment_cover_recycler.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler.CoverRecyclerAdapter
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view_model.CoverRecyclerViewModel


class CoverRecyclerFragment: Fragment(),
    ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnItemClickListener,
    CoverRecyclerAdapter.OnShareClickListener, View.OnClickListener, Searchable {

    private lateinit var adapter: CoverRecyclerAdapter
    private lateinit var onItemClickListener: CoverRecyclerAdapter.OnItemClickListener
    private lateinit var chapterNameDialog: ChapterNameDialog
    private lateinit var viewModel: CoverRecyclerViewModel

    private val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && fabRecycler.isShown)
                fabRecycler.hide()
            else
                fabRecycler.show()
        }
    }

    private var isFloatPanelOpen = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CoverRecyclerViewModel::class.java)
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

    override fun onShareClick(text: String) {
        GlobalScope.launch {
            val diaryDao = App.instance.db.diaryDao()
            val pages = diaryDao.getPagesByChapterName(text)
            val pdfPath = try {
                val rootDir = App.instance.getRootDir()
                if (rootDir == null)
                    throw IOException()
                else
                    viewModel.createPdf(pages, "$rootDir/$text.pdf")
            } catch (e: IOException) {
                null
            }
            if (pdfPath == null)
                withContext(Dispatchers.Main){
                    context?.toast(resources.getString(R.string.empty_document))
                }
            else {
                val intent = Intent(Intent.ACTION_SEND)

                val apkURI = FileProvider.getUriForFile(context!!,
                    context?.applicationContext?.packageName + ".provider", File(pdfPath))
                intent.putExtra(Intent.EXTRA_STREAM, apkURI)
                intent.type = "application/pdf"
                withContext(Dispatchers.Main) {
                    try {
                        startActivity(Intent.createChooser(intent, resources.getString(R.string.share_with)))
                    } catch (e: ActivityNotFoundException) {
                        context?.toast(resources.getString(R.string.no_app_to_open_pdf) + pdfPath)
                    }
                }
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

    private fun createPdf(chapter: DiaryChapter){
        val pathName = "${context!!.filesDir}/${chapter.name}.$PDF_EXT"
        chapter.pdfPath = pathName
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(pathName))
        document.open()
        document.add(Paragraph(" "))
        document.close()
    }

    override fun searchByDate(text: String) {
        adapter.filter(text)
    }

    override fun onItemClick(text: String) {
        onItemClickListener.onItemClick(text)
    }

    companion object {
        fun newInstance(onItemClickListener: CoverRecyclerAdapter.OnItemClickListener): CoverRecyclerFragment {
            val fragment = CoverRecyclerFragment()
            fragment.onItemClickListener = onItemClickListener
            return fragment
        }
    }
}