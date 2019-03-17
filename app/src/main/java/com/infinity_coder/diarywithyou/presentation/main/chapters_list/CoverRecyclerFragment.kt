package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.data.db.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.fragment_cover_recycler.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CoverRecyclerFragment: Fragment(),
    ChapterNameDialog.OnChapterNameDialogListener,
    CoverRecyclerAdapter.OnItemClickListener,
    CoverRecyclerAdapter.OnShareClickListener, View.OnClickListener, Searchable {

    lateinit var adapter: CoverRecyclerAdapter
    lateinit var onItemClickListener: CoverRecyclerAdapter.OnItemClickListener
    lateinit var chapterNameDialog: ChapterNameDialog
    lateinit var viewModel: CoverRecyclerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CoverRecyclerViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cover_recycler, container, false)
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
                    Toast.makeText(context, "Документ пуст", Toast.LENGTH_SHORT).show()
                }
            else {
                val intent = Intent(Intent.ACTION_SEND)

                val apkURI = FileProvider.getUriForFile(context!!,
                    context?.applicationContext?.packageName + ".provider", File(pdfPath))
                intent.putExtra(Intent.EXTRA_STREAM, apkURI)
                intent.type = "application/pdf"
                withContext(Dispatchers.Main) {
                    try {
                        startActivity(Intent.createChooser(intent, "Choose app"))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "У Вас нет программы для просмотра файла. \nПуть к системному файлу: $pdfPath",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
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
        fun newInstance(onItemClickListener: CoverRecyclerAdapter.OnItemClickListener): CoverRecyclerFragment {
            val fragment = CoverRecyclerFragment()
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