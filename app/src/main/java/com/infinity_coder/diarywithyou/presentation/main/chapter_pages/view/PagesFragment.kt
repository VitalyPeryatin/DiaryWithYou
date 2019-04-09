package com.infinity_coder.diarywithyou.presentation.main.chapter_pages.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.infinity_coder.diarywithyou.App
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.*
import com.infinity_coder.diarywithyou.presentation.camera.CameraActivity
import com.infinity_coder.diarywithyou.presentation.main.MainActivity
import com.infinity_coder.diarywithyou.presentation.main.Searchable
import com.infinity_coder.diarywithyou.presentation.main.chapter_pages.view.recycler.PagesRecyclerAdapter
import com.infinity_coder.diarywithyou.utils.PdfCreator
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PagesFragment: Fragment(), Searchable {
    private var chapterName: String? = null
    private val adapter: PagesRecyclerAdapter by lazy { PagesRecyclerAdapter(this, chapterName) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adapter.addViewWithActionBar(activity as AppCompatActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chapterName = arguments?.getString(CHAPTER_KEY)

        LinearLayoutManager(context)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerPages.layoutManager = layoutManager
        recyclerPages.adapter = adapter

        fabCamera.setOnClickListener {
            if(hasCameraPermissions()) {
                (activity as MainActivity).closeSearchView()
                startActivityForResult(Intent(context, CameraActivity::class.java), CAMERA_REQUEST_CODE)
            }else{
                requestPermissions(cameraPermissions, CAMERA_PERMISSION_CODE)
            }
        }
    }

    /**
     * Создаёт pdf файл и открывает его через стороннее приложение
     */
    fun openPdf(){
        val pdfPath = try {
            val rootDir = App.instance.getRootDir()
            if(rootDir == null)
                throw IOException()
            else
                PdfCreator.createPdf(adapter.getPages(), "$rootDir/$chapterName.pdf")
        }catch (e: IOException){ null }
        if(pdfPath == null)
            Toast.makeText(context, resources.getString(R.string.empty_document), Toast.LENGTH_SHORT).show()
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
            } catch (e: Throwable) {
                Toast.makeText(
                    context,
                    "${resources.getString(R.string.no_app_to_open_pdf)}$pdfPath", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as MainActivity).optionsMenu.postValue(MainActivity.MenuType.PAGES)
        (activity as MainActivity).activeFragment = this
    }

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)

    private fun hasCameraPermissions(): Boolean = activity!!.isPermissionsGranted(cameraPermissions)

    /**
     * Проеряет разрешение для досутпа к внешним файлам, при положительном ответе создаёт во
     * внешней директории pdf-файл
     *
     * Также проверяет разрешение на камеру, при положительном ответе открывает камеру
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
            if(grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                openPdf()
        }
        else if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                fabCamera.performClick()
            }
            else {
                showRationale(R.string.permission_camera_rationale)
            }
        }
    }

    private fun showRationale(@StringRes stringId: Int){
        val snackBar = Snackbar.make(rootLayout, stringId, Snackbar.LENGTH_LONG)
        snackBar.setAction(R.string.settings) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity?.packageName, null))
            startActivity(intent)
            snackBar.dismiss()
        }.show()
    }

    /**
     * Добавляет новую страницу в адаптер, полученную с камеры
     */
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            val imagePath = data?.getStringExtra("imagePath")!!
            val date = SimpleDateFormat("dd.MM.yyyy").format(Date())
            adapter.addPage(chapterName!!, imagePath, date)
            GlobalScope.launch {
                delay(800)
                recyclerPages.smoothScrollToPosition(adapter.itemCount-1)
            }
        }
    }

    /**
     * Ищет страницы по дате, введённой в виде строки
     */
    override fun searchByDate(text: String) {
        adapter.filter(text)
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).activeFragment = null
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.removeViewWithActionBar()
    }

    companion object {
        fun newInstance(text: String): PagesFragment {
            val fragment = PagesFragment()
            val bundle = Bundle()
            bundle.putString(CHAPTER_KEY, text)
            fragment.arguments = bundle
            return fragment
        }
    }
}