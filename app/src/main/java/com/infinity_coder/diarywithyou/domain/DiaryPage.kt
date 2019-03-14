package com.infinity_coder.diarywithyou.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DiaryPage(
    var chapter: String,
    var imagePath: String,
    var date: String){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}