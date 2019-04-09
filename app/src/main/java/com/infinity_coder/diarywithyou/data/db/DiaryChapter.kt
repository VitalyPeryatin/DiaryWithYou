package com.infinity_coder.diarywithyou.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
data class DiaryChapter(
    @ColumnInfo(name = "name")
    var name: String,
    var pdfPath: String,
    var coverPath: String?
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}