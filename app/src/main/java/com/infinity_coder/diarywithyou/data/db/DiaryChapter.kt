package com.infinity_coder.diarywithyou.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DiaryChapter(
    @PrimaryKey
    var name: String,
    var pdfPath: String,
    var coverPath: String?
)