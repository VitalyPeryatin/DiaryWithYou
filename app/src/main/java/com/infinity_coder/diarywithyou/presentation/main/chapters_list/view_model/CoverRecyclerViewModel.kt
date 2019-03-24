package com.infinity_coder.diarywithyou.presentation.main.chapters_list.view_model

import androidx.lifecycle.ViewModel
import com.infinity_coder.diarywithyou.data.db.DiaryPage
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import com.itextpdf.text.PageSize



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
                image.scaleToFit(0.95f * PageSize.A4.width, 0.95f * PageSize.A4.height)
                val x = (PageSize.A4.width - image.scaledWidth) / 2
                val y = (PageSize.A4.height - image.scaledHeight) / 2
                image.setAbsolutePosition(x, y - 30)
                document.add(image)
            }
            document.close()
        }catch (e: Exception){
            return@runBlocking null
        }
        return@runBlocking pdfPath
    }
}
