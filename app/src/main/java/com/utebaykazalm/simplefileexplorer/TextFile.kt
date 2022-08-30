package com.utebaykazalm.simplefileexplorer

data class TextFile(
    val fileName: String,
    val path: String = "", //Не знаю какое именно нужно
    val content: String = ""
)