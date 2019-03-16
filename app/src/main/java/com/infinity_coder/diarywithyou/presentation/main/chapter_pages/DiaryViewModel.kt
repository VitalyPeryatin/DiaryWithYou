package com.infinity_coder.diarywithyou.presentation.main.chapter_pages

import android.os.Environment
import androidx.lifecycle.ViewModel
import com.infinity_coder.diarywithyou.domain.DiaryPage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.FileOutputStream
import java.util.*
import kotlin.Exception

class DiaryViewModel: ViewModel() {
    fun createPdf(pages: List<DiaryPage>, parentDir: String): String? = runBlocking (Dispatchers.IO){
        val pdfPath = "$parentDir/${UUID.randomUUID()}.pdf"
        try {
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfPath))

            document.open()
            val font1 = Font(
                Font.FontFamily.HELVETICA,
                16F, Font.BOLD)

            for (page in pages) {
                document.newPage()
                val title = Paragraph(page.date, font1)
                title.alignment = Element.ALIGN_CENTER
                title.spacingAfter = 16F
                document.add(title)

                val image = Image.getInstance(page.imagePath)
                document.add(image)
            }
            document.close()
        }catch (e: Exception){
            return@runBlocking null
        }
        return@runBlocking pdfPath
    }
}