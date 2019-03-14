package com.infinity_coder.diarywithyou.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.main.chapter_pages.DiaryFragment
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.CoverRecyclerAdapter
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.DiaryRecyclerFragment
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
import java.util.*


class MainActivity : AppCompatActivity(),
    CoverRecyclerAdapter.OnItemClickListener {

    val EXTERNAL_STORAGE_PERMISSION_CODE = 1
    lateinit var diaryRecyclerFragment: DiaryRecyclerFragment
    var activeFragment: Fragment? = null
    lateinit var searchView: SearchView

    enum class MenuType{CHAPTERS, PAGES}

    val optionsMenu = MutableLiveData<MenuType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        diaryRecyclerFragment =
            DiaryRecyclerFragment.newInstance(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_place, diaryRecyclerFragment)
            .commit()
        optionsMenu.observe(this, Observer<MenuType>{
            invalidateOptionsMenu()
        })
    }

    override fun onItemClick(text: String) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_place,
                DiaryFragment.newInstance(text)
            )
            .addToBackStack(null)
            .commit()
        closeSearchView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(optionsMenu.value == MenuType.CHAPTERS) {
            menuInflater.inflate(R.menu.menu_app_bar_chapters, menu)
        }
        else if(optionsMenu.value == MenuType.PAGES){
            menuInflater.inflate(R.menu.menu_app_bar_pages, menu)
        }
        searchView = menu?.findItem(R.id.app_bar_search)?.actionView as SearchView
        searchView.queryHint = "Search"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@MainActivity, "Готово: $query", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (activeFragment is Searchable) {
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
            R.id.app_bar_pdf -> {
                if(activeFragment is DiaryFragment){
                    (activeFragment as DiaryFragment).createPdf()
                }
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

    fun closeSearchView(){
        toolbar.collapseActionView()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            //if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED)
                // createPdf()
        }
    }
}
