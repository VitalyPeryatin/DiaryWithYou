package com.infinity_coder.diarywithyou.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.infinity_coder.diarywithyou.R
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.net.URL


class MainActivity : AppCompatActivity(),
    CoverRecyclerAdapter.OnItemClickListener {

    val EXTERNAL_STORAGE_PERMISSION_CODE = 1
    lateinit var diaryRecyclerFragment: DiaryRecyclerFragment
    lateinit var activeFragment: Fragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        diaryRecyclerFragment =
            DiaryRecyclerFragment.newInstance(this)
        activeFragment = diaryRecyclerFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_place, activeFragment)
            .commit()
    }

    override fun onItemClick(text: String) {
        activeFragment = DiaryFragment.newInstance(text)
        diaryRecyclerFragment.hideFab()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_place,
                activeFragment
            )
            .addToBackStack(null)
            .commit()
    }

    lateinit var searchView: SearchView

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_bar, menu)
        searchView = menu?.findItem(R.id.app_bar_search)?.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {

            override fun onViewDetachedFromWindow(arg0: View) {
                diaryRecyclerFragment.showFab()
            }

            override fun onViewAttachedToWindow(arg0: View) {
                diaryRecyclerFragment.hideFab()
            }
        })
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@MainActivity, "Готово: $query", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let{
                    if(activeFragment is Searchable){
                        (activeFragment as Searchable).search(newText)
                    }
                }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.app_bar_search -> {

            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val permissionStatusRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionStatusWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionStatusCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//        if (permissionStatusRead == PackageManager.PERMISSION_GRANTED && permissionStatusWrite == PERMISSION_GRANTED) {
//            createPdf()
//        }
        if(permissionStatusRead == PackageManager.PERMISSION_DENIED ||
            permissionStatusWrite == PackageManager.PERMISSION_DENIED ||
                permissionStatusCamera == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA),
                EXTERNAL_STORAGE_PERMISSION_CODE
            )

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            //if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED)
                // createPdf()
        }
    }

    fun createPdf(){
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream("${Environment.getExternalStorageDirectory().absoluteFile}/template3.pdf"))
        GlobalScope.launch(Dispatchers.IO) {
            document.open()
            val font1 = Font(
                Font.FontFamily.HELVETICA,
                16F, Font.BOLD
            )
            val title = Paragraph("Receipt", font1)
            title.alignment = Element.ALIGN_CENTER
            title.spacingAfter = 16F
            document.add(title)
            val imageUrl = "http://www.javenue.info/files/sample.png"
            val stamp = Image.getInstance(URL(imageUrl))
            document.add(stamp)
            document.close()

            val reader = PdfReader("${Environment.getExternalStorageDirectory().absoluteFile}/template3.pdf")
            val strategy = SimpleTextExtractionStrategy()
            for(page in 1..reader.numberOfPages){
                val text = PdfTextExtractor.getTextFromPage(reader, page, strategy)
                Log.d("myLog", text)
            }
            reader.close()
        }
    }
}
