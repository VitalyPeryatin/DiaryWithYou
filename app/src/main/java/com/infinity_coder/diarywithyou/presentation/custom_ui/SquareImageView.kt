package com.infinity_coder.diarywithyou.presentation.custom_ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Обрезает изображение в форме квадрата.
 */
class SquareImageView(context: Context, attributeSet: AttributeSet?): ImageView(context, attributeSet) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}