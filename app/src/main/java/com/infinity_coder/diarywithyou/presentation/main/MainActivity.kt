package com.infinity_coder.diarywithyou.presentation.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.main.chapter_pages.view.PagesFragment
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.ChaptersFragment
import com.infinity_coder.diarywithyou.presentation.main.chapters_list.view.recycler.CoverRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Основной экран, содержащий в себе все фрагменты по работе с ежедневником
 */
class MainActivity : AppCompatActivity(), CoverRecyclerAdapter.OnChapterClickListener {

    var activeFragment: Fragment? = null
    val optionsMenu = MutableLiveData<MenuType>()
    private lateinit var diaryRecyclerFragment: ChaptersFragment
    private lateinit var searchView: SearchView
    enum class MenuType{CHAPTERS, PAGES}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Устанавливает начальный фрагмент на основной экран
        diaryRecyclerFragment = ChaptersFragment.newInstance(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_place, diaryRecyclerFragment)
            .commit()

        optionsMenu.observe(this, Observer<MenuType>{
            invalidateOptionsMenu()
        })
    }

    /**
     * Перезодит на новый фрагмент со страницами выбранной главы
     */
    override fun onChapterClick(text: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_place, PagesFragment.newInstance(text))
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
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (activeFragment is Searchable) {
                        (activeFragment as Searchable).searchByDate(newText)
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
                if(activeFragment is PagesFragment){
                    (activeFragment as PagesFragment).openPdf()
                }
            }
            android.R.id.home -> onBackPressed()
        }
        return true
    }

    fun closeSearchView(){
        toolbar.collapseActionView()
    }
}
