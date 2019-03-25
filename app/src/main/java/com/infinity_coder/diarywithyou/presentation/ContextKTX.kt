package com.infinity_coder.diarywithyou.presentation

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Activity.isPermissionsGranted(permissions: Array<String>): Boolean{
    var isValid = true
    for(permission in permissions){
        isValid = isValid && ContextCompat.checkSelfPermission(baseContext, permission) == PackageManager.PERMISSION_GRANTED
        if(!isValid) return false
    }
    return isValid
}

fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}