package com.infinity_coder.diarywithyou.presentation.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.domain.DiaryChapter
import com.infinity_coder.diarywithyou.presentation.camera.CameraActivity
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.xml.transform.Result

class DiaryFragment: Fragment() {

    lateinit var diaryChapter: DiaryChapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chapterName = arguments?.getString("chapter")

        val diaryDao = App.instance.db.diaryDao()
        GlobalScope.launch(Dispatchers.IO) {
            diaryChapter = diaryDao.getByName(chapterName!!)

            pdfView.fromFile(File(diaryChapter.pdfPath)).load()
        }
        fabCamera.setOnClickListener {
            startActivityForResult(Intent(context, CameraActivity::class.java), 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode == RESULT_OK){
            addImageToPdf(data?.getStringExtra("imagePath")!!, diaryChapter.name)
        }
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