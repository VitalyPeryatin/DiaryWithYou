package com.infinity_coder.diarywithyou.presentation.main.chapter_pages

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.camera.CameraActivity
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import kotlinx.android.synthetic.main.fragment_diary.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.fragment_diary_recycler.*
import kotlinx.coroutines.*

class DiaryFragment: Fragment(), Searchable {
    var chapterName: String? = null
    lateinit var adapter: PagesRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chapterName = arguments?.getString("chapter")

        adapter = PagesRecyclerAdapter(this, chapterName)
            LinearLayoutManager(context)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerPages.layoutManager = layoutManager
        recyclerPages.adapter = adapter

//        GlobalScope.launch(Dispatchers.IO) {
//            chapterName = diaryDao.getByName(chapterName!!)
//
//            pdfView.fromFile(File(chapterName.pdfPath)).load()
//        }
        fabCamera.setOnClickListener {
            (activity as MainActivity).closeSearchView()
            startActivityForResult(Intent(context, CameraActivity::class.java), 1)
        }
    }

    fun createPdf(){
        val document = Document()
        val pdfPath = "${Environment.getExternalStorageDirectory().absoluteFile}/${UUID.randomUUID()}.pdf"
        PdfWriter.getInstance(document, FileOutputStream(pdfPath))
        val pages = adapter.getPages()
        GlobalScope.launch(Dispatchers.IO) {
            document.open()

            val font1 = Font(
                Font.FontFamily.HELVETICA,
                16F, Font.BOLD
            )

            for(page in pages) {
                document.newPage()
                val title = Paragraph(page.date, font1)
                title.alignment = Element.ALIGN_CENTER
                title.spacingAfter = 16F
                document.add(title)

                val image = Image.getInstance(page.imagePath)
                document.add(image)
            }
            document.close()

            val intent = Intent(Intent.ACTION_VIEW)

            val apkURI = FileProvider.getUriForFile(context!!,
                context?.applicationContext?.packageName + ".provider", File(pdfPath))
            intent.setDataAndType(apkURI, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try{
                startActivity(intent)
            } catch(e: ActivityNotFoundException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "У Вас нет программы для просмотра файла. \nПуть к системному файлу: $pdfPath",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).optionsMenu.postValue(MainActivity.MenuType.PAGES)
        (activity as MainActivity).activeFragment = this
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).activeFragment = null
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == RESULT_OK){
            val imagePath = data?.getStringExtra("imagePath")!!
            val date = SimpleDateFormat("dd.MM.yyyy").format(Date())
            adapter.addPage(chapterName!!, imagePath, date)
            GlobalScope.launch {
                delay(800)
                recyclerPages.smoothScrollToPosition(adapter.itemCount-1)
            }
        }
    }

    override fun search(text: String) {
        adapter.filter(text)
    }

    fun addImageToPdf(imagePath: String, pdfName: String){
        val newDocument = Document()
        PdfWriter.getInstance(newDocument, FileOutputStream("${Environment.getExternalStorageDirectory().absoluteFile}/newPage.pdf"))
        GlobalScope.launch(Dispatchers.IO) {
            newDocument.open()
            val font1 = Font(
                Font.FontFamily.HELVETICA,
                16F, Font.BOLD
            )
            val title = Paragraph("Receipt", font1)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 16F
            newDocument.add(title)

            val image = Image.getInstance(imagePath)
            newDocument.add(image)
            newDocument.close()

            val pdfFile = File("${Environment.getExternalStorageDirectory().absoluteFile}/$pdfName.pdf")
            val pdfFileCopy = File("${Environment.getExternalStorageDirectory().absoluteFile}/${pdfName}_copy.pdf")
            pdfFile.renameTo(pdfFileCopy)
            val reader1 = PdfReader("${Environment.getExternalStorageDirectory().absoluteFile}/newPage.pdf")
            val reader2 = PdfReader("${Environment.getExternalStorageDirectory().absoluteFile}/${pdfName}_copy.pdf")
            val document = Document()
            val fos = FileOutputStream("${Environment.getExternalStorageDirectory().absoluteFile}/${pdfFile.name}")
            val copy = PdfCopy(document, fos)
            document.open()
            var page: PdfImportedPage
            var stamp: PdfCopy.PageStamp
            var phrase: Phrase
            val bf = BaseFont.createFont()
            val font = Font(bf, 9f)
            val n = reader1.numberOfPages
            for (i in 1..reader1.numberOfPages) {
                page = copy.getImportedPage(reader1, i)
                stamp = copy.createPageStamp(page)
                phrase = Phrase("page $i", font)
                ColumnText.showTextAligned(stamp.overContent, Element.ALIGN_CENTER, phrase, 520f, 5f, 0f)
                stamp.alterContents()
                copy.addPage(page)
            }
            for (i in 1..reader2.numberOfPages) {
                page = copy.getImportedPage(reader2, i)
                stamp = copy.createPageStamp(page)
                phrase = Phrase("page " + (n + i), font)
                ColumnText.showTextAligned(stamp.overContent, Element.ALIGN_CENTER, phrase, 520f, 5f, 0f)
                stamp.alterContents()
                copy.addPage(page)
            }
            val fileNewPage = File("${Environment.getExternalStorageDirectory().absoluteFile}/newPage.pdf")
            fileNewPage.delete()
            pdfFileCopy.delete()
            document.close()
            reader1.close()
            reader2.close()
        }
    }

    companion object {
        fun newInstance(text: String): DiaryFragment {
            val fragment = DiaryFragment()
            val bundle = Bundle()
            bundle.putString("chapter", text)
            fragment.arguments = bundle
            return fragment
        }
    }
}