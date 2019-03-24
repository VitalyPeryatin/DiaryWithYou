package com.infinity_coder.diarywithyou.presentation.custom_ui

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/**
 * Устанавливает размеры CardView в формат листа A4.
 */
class CardViewA4(context: Context, attributeSet: AttributeSet?): CardView(context, attributeSet) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = (width * Math.sqrt(2.toDouble())).toInt() // width * √2
        setMeasuredDimension(width, height)
    }
}