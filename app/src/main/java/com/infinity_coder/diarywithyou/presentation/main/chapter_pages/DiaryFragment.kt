package com.infinity_coder.diarywithyou.presentation.main.chapter_pages

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import androidx.lifecycle.ViewModelProviders
import com.infinity_coder.diarywithyou.presentation.EXTERNAL_STORAGE_PERMISSION_CODE
import com.infinity_coder.diarywithyou.presentation.isPermisssionsGranted
import kotlinx.coroutines.*
import java.io.IOException


class DiaryFragment: Fragment(), Searchable {
    var chapterName: String? = null
    lateinit var adapter: PagesRecyclerAdapter
    lateinit var viewModel: DiaryViewModel



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DiaryViewModel::class.java)
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

        fabCamera.setOnClickListener {
            (activity as MainActivity).closeSearchView()
            startActivityForResult(Intent(context, CameraActivity::class.java), 1)
        }
    }

    fun createAndOpenPdf(){
        val pdfPath = try {
            viewModel.createPdf(adapter.getPages(), context!!.filesDir.path)
        }catch (e: IOException){ null }
        if(pdfPath == null)
            Toast.makeText(context, "Документ пуст", Toast.LENGTH_SHORT).show()
        else {
            val intent = Intent(Intent.ACTION_VIEW)

            val apkURI = FileProvider.getUriForFile(
                context!!,
                context?.applicationContext?.packageName + ".provider", File(pdfPath)
            )
            intent.setDataAndType(apkURI, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "У Вас нет программы для просмотра файла. \nПуть к системному файлу: $pdfPath", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /*fun createPdfWithPermissions(){
        val permissions = arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
        if(!isPermisssionsGranted(permissions))
            ActivityCompat.requestPermissions(activity!!, permissions, EXTERNAL_STORAGE_PERMISSION_CODE)
        else
            createAndOpenPdf()
    }*/



    override fun onStart() {
        super.onStart()
        (activity as MainActivity).optionsMenu.postValue(MainActivity.MenuType.PAGES)
        (activity as MainActivity).activeFragment = this
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).activeFragment = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                createAndOpenPdf()
        }
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