package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
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
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler.CoverRecyclerAdapter
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
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import com.google.android.material.snackbar.Snackbar
import com.infinity_coder.diarywithyou.presentation.*


class ChaptersFragment: Fragment(), ICoverEditor,
    ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnChapterClickListener,
    CoverRecyclerAdapter.OnShareClickListener, View.OnClickListener, Searchable {

    private lateinit var adapter: CoverRecyclerAdapter
    private lateinit var onChapterClickListener: CoverRecyclerAdapter.OnChapterClickListener
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
            arrowDropDownUp.animate().rotationBy(180f).rotation(0f)
            frameSiteLink.visibility = GONE
        }
        else {
            arrowDropDownUp.animate().rotation(180f)
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
                this,
                this
            )

        recyclerCover.layoutManager = GridLayoutManager(context, 2)
        recyclerCover.adapter = adapter
        recyclerCover.addOnScrollListener(onScrollListener)

        fabRecycler.setOnClickListener(this)
        btnDropDown.setOnClickListener(dropDownUpListener)

        frameSiteLink.visibility = GONE

        tvSiteLink.setOnClickListener {
            btnDropDown.performClick()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(R.string.diary_site_link))))
        }
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
                if(hasPermissions()) {
                    ChapterNameDialog.newInstance(this).show(childFragmentManager, DIALOG_FRAGMENT_KEY)
                    (activity as MainActivity).closeSearchView()
                }else
                    requestPermissions(storagePermissions, EXTERNAL_STORAGE_PERMISSION_CODE)
            }
        }
    }

    override fun editChapter(chapter: DiaryChapter) {
        ChapterNameDialog.newInstance(this, chapter).show(childFragmentManager, DIALOG_FRAGMENT_KEY)
        (activity as MainActivity).closeSearchView()
    }

    override fun updateChapter(chapter: DiaryChapter) {
        chapterInteractor.updateChapter(chapter)
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

    private val storagePermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private fun hasPermissions(): Boolean = activity!!.isPermissionsGranted(storagePermissions)

    /**
     * Проверяет предоставлены ли разрешения на использование внешней памяти
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE){
            if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                adapter.notifyDataSetChanged()
                fabRecycler.performClick()
            }
            else {
                showRationale()
            }
        }
    }

    private fun showRationale(){
        val snackBar = Snackbar.make(rootLayout, R.string.permission_storage_rationale, Snackbar.LENGTH_LONG)
        snackBar.setAction(R.string.settings) {
            val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity?.packageName, null))
            startActivity(intent)
            snackBar.dismiss()
        }.show()
    }

    companion object {
        fun newInstance(onChapterClickListener: CoverRecyclerAdapter.OnChapterClickListener): ChaptersFragment {
            val fragment = ChaptersFragment()
            fragment.onChapterClickListener = onChapterClickListener
            return fragment
        }
    }
}