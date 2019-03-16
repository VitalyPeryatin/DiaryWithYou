package com.infinity_coder.diarywithyou.presentation.main.chapters_list

import android.graphics.Bitmap
import com.squareup.picasso.Transformation


class CropSquareTransformation : Transformation {

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    override fun transform(source: Bitmap): Bitmap {
        val size = Math.min(source.width, source.height)

        mWidth = (source.width - size) / 2
        mHeight = (source.height - size) / 2

        val bitmap = Bitmap.createBitmap(source, mWidth, mHeight, size, size)
        if (bitmap != source) {
            source.recycle()
        }
        return bitmap
    }

    override fun key(): String {
        return "CropSquareTransformation(width=$mWidth, height=$mHeight)"
    }
}