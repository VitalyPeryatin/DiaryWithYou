package com.infinity_coder.diarywithyou.presentation.camera

import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import java.io.FileOutputStream
import java.util.*

class CameraViewModel: ViewModel() {
    fun saveBitmapTo(bitmap: Bitmap?, parentDir: String): String {
        var out: FileOutputStream? = null
        val pathName = "$parentDir/image-${UUID.randomUUID()}.png"
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
}