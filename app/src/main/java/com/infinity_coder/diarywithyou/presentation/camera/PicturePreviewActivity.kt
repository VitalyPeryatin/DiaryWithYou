package com.infinity_coder.diarywithyou.presentation.camera

import android.app.Activity
import android.os.Bundle
import com.infinity_coder.diarywithyou.R
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.activity_picture_preview.*


class PicturePreviewActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_preview)
        val result = image
        if (result == null) {
            finish()
            return
        }
        result.toBitmap(
            1000, 1000
        ) { bitmap -> imageView.setImageBitmap(bitmap) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            setPictureResult(null)
        }
    }

    companion object {
        private var image: PictureResult? = null
        fun setPictureResult(im: PictureResult?) {
            image = im
        }
    }
}
