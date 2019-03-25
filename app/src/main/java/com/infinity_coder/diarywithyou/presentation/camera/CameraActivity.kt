package com.infinity_coder.diarywithyou.presentation.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.infinity_coder.diarywithyou.R
import com.infinity_coder.diarywithyou.presentation.CAMERA_PERMISSION_CODE
import com.infinity_coder.diarywithyou.presentation.IMAGE_PATH_KEY
import com.infinity_coder.diarywithyou.presentation.toast
import com.otaliastudios.cameraview.*
import kotlinx.android.synthetic.main.activity_camera.*


/**
 * Отображает Activity для получения изображения с камеры.
 */
class CameraActivity: AppCompatActivity(){

    private val viewModel: CameraViewModel by lazy { ViewModelProviders.of(this).get(CameraViewModel::class.java) }
    private val maxBitmapWidth = 5000
    private val maxBitmapHeight = 5000

    // Прослушивает событие, когда фото готово к отображению
    private val cameraListener = object : CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            onPicture(result)
        }

        override fun onCameraError(exception: CameraException) {
            toast("${resources.getString(R.string.camera_exception)}${exception.reason}")
        }
    }

    // Прослушивает событие, когда пользователь начал делать фото
    private val capturePictureListener = View.OnClickListener {
        if (cameraView.mode == Mode.PICTURE) {
            toast(resources.getString(com.infinity_coder.diarywithyou.R.string.capturing_picture))
            cameraView.takePicture()
        }
        else
            toast("${resources.getString(com.infinity_coder.diarywithyou.R.string.camera_mode)}${cameraView.mode}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.infinity_coder.diarywithyou.R.layout.activity_camera)

        setCameraView()
        fabCamera.setOnClickListener(capturePictureListener)
    }

    /**
     * Устанавливает конфигурацию camera view.
     */
    private fun setCameraView(){
        cameraView.setLifecycleOwner(this)
        cameraView.mode = Mode.PICTURE
        cameraView.audio = Audio.OFF
        cameraView.addCameraListener(cameraListener)
    }

    /**
     * Трансформирует изображение, полученное с камеры, в Bitmap и возвращает Bitmap как резудьтат
     * в предыдущее Activity
     */
    private fun onPicture(result: PictureResult) {
        result.toBitmap(maxBitmapWidth, maxBitmapHeight) { bitmap ->
            if(bitmap != null) {
                val matrix = Matrix()
                matrix.postRotate(270f)
                val verticalBitmap = if(bitmap.width > bitmap.height)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                else
                    bitmap

                val imagePath = viewModel.saveBitmapToDir(verticalBitmap, "$filesDir")
                val intent = Intent()
                intent.putExtra(IMAGE_PATH_KEY, imagePath)
                setResult(Activity.RESULT_OK, intent)
            }
            else{
                setResult(Activity.RESULT_CANCELED, Intent())
            }
            finish()
        }
    }

    /**
     * Получает разрешение для использования камеры. При положительном ответе пользователя открывает камеру.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && !cameraView.isOpened)
                cameraView.open()
            else
                finish()
        }
    }
}
