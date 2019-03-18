package com.infinity_coder.diarywithyou.presentation.main

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.EXTERNAL_STORAGE_PERMISSION_CODE
import com.infinity_coder.diarywithyou.presentation.isPermisssionsGranted
import com.infinity_coder.diarywithyou.presentation.main.chapter_pages.DiaryFragment
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.CoverRecyclerAdapter
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.CoverRecyclerFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), CoverRecyclerAdapter.OnItemClickListener {

    var activeFragment: Fragment? = null
    val optionsMenu = MutableLiveData<MenuType>()
    private lateinit var diaryRecyclerFragment: CoverRecyclerFragment
    private lateinit var searchView: SearchView
    enum class MenuType{CHAPTERS, PAGES}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        diaryRecyclerFragment = CoverRecyclerFragment.newInstance(this)
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

    override fun onStart() {
        super.onStart()
        requestPermissionsIfNeed()
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
                    (activeFragment as DiaryFragment).createAndOpenPdf()
                }
            }
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    fun requestPermissionsIfNeed(){
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(!isPermisssionsGranted(permissions))
            ActivityCompat.requestPermissions(this, permissions, EXTERNAL_STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE){
            requestPermissionsIfNeed()
        }
    }

    fun closeSearchView(){
        toolbar.collapseActionView()
    }
}
