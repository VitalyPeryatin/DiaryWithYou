package com.infinity_coder.diarywithyou.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DiaryChapter(
    var name: String,
    var pdfPath: String,
    var coverPath: String?
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}