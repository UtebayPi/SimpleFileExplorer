package com.utebaykazalm.simplefileexplorer.data

import com.utebaykazalm.simplefileexplorer.utils.Resource

data class TextFile(
    val fileName: String,
    val content: String,
    val path: String = "" //Не знаю какое именно нужно. Возможно лучше Uri.
) {
    fun trimVerifyCreate(): Resource<TextFile> {
        val trimName = fileName.trim()
        val fixedContent = content.trim()
        if (trimName.isBlank() or fixedContent.isBlank()) return Resource.Error("Content or name is empty")
        val fixedName = when {
            trimName.endsWith(".txt") -> trimName
            trimName.contains(".") -> return Resource.Error("Name contains \".\" symbol, and not proper \".txt\" ending")
            else -> "$trimName.txt"
        }
        return Resource.Success(TextFile(fixedName, fixedContent))
    }
}