package com.utebaykazalm.simplefileexplorer.data

import com.utebaykazalm.simplefileexplorer.utils.Resource

data class TextFile(
    val fileName: String,
    val content: String = "",
    val path: String = "" //Не знаю какое именно нужно
) {

}