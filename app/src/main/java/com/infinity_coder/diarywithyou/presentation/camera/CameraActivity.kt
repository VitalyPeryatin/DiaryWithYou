package com.infinity_coder.diarywithyou.presentation.camera

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.infinity_coder.diarywithyou.R
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*
import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.infinity_coder.diarywithyou.presentation.IMAGE_PATH_KEY
import com.infinity_coder.diarywithyou.presentation.toast


class CameraActivity: AppCompatActivity(){

    lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

        cameraView.setLifecycleOwner(this)
        cameraView.mode = Mode.PICTURE
        cameraView.audio = Audio.OFF
        cameraView.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {}

            override fun onPictureTaken(result: PictureResult) {
                onPicture(result)
            }

            override fun onCameraError(exception: CameraException) {
                toast("Got CameraException #${exception.reason}")
            }
        })

        fabCamera.setOnClickListener(capturePictureListener)
    }

    private fun onPicture(result: PictureResult) {
        result.toBitmap(1000, 1000) { bitmap ->
            val imagePath = viewModel.saveBitmapTo(bitmap, "$filesDir")
            val intent = Intent()
            intent.putExtra(IMAGE_PATH_KEY, imagePath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 16) {
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && !cameraView.isOpened)
                cameraView.open()
            else
                finish()
        }
    }



    private val capturePictureListener = View.OnClickListener {
        if (cameraView.mode == Mode.PICTURE) {
            toast("Capturing picture...")
            cameraView.takePicture()
        }
        else
            toast("Камера в режиме ${cameraView.mode}")
    }
}
