package com.infinity_coder.diarywithyou.presentation.camera

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.infinity_coder.diarywithyou.R
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import android.content.Intent




class CameraActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)

        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                onOpened(options)
            }

            override fun onPictureTaken(result: PictureResult) {
                onPicture(result)
            }

            override fun onCameraError(exception: CameraException) {
                onError(exception)
            }
        })

        btnCamera.setOnClickListener {
            capturePicture()
        }
    }

    private fun message(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

    private fun onOpened(options: CameraOptions) {

    }

    private fun onError(exception: CameraException) {
        message("Got CameraException #" + exception.reason)
    }

    private fun onPicture(result: PictureResult) {
        result.toBitmap(1000, 1000) { bitmap ->
            val imagePath = saveBitmap(bitmap)
            val intent = Intent()
            intent.putExtra("imagePath", imagePath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
//        PicturePreviewActivity.setPictureResult(result)
//        startActivity(Intent(this@CameraActivity, PicturePreviewActivity::class.java))
    }

    private fun capturePicture() {
        if (cameraView.mode == Mode.PICTURE) {
            message("Capturing picture...")
            cameraView.takePicture()
        }
    }

    fun saveBitmap(bitmap: Bitmap?): String {
        var out: FileOutputStream? = null
        val pathName = "${Environment.getExternalStorageDirectory()}/DCIM/100ANDRO/${UUID.randomUUID()}.png"
        try {
            out = FileOutputStream(pathName)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            out?.close()
        }
        return pathName
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var valid = true
        for (grantResult in grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
        }
        if (valid && !cameraView.isOpened) {
            cameraView.open()
        }
    }
}
