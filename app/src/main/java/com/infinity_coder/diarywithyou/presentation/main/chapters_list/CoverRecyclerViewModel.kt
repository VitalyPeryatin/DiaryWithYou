package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import androidx.lifecycle.ViewModel
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

class CoverRecyclerViewModel: ViewModel() {
    fun createPdf(pages: List<DiaryPage>, pdfPath: String): String? = runBlocking (Dispatchers.IO){
        try {
            val file = File(pdfPath)
            if(file.exists())
                file.delete()
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(pdfPath))

            document.open()
            val font1 = Font(
                Font.FontFamily.HELVETICA,
                16F, Font.BOLD)

            for (page in pages.reversed()) {
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