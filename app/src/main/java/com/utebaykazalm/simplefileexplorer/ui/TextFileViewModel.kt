package com.utebaykazalm.simplefileexplorer.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utebaykazalm.simplefileexplorer.data.TextFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextFileViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {

    private var _textFiles: MutableStateFlow<List<TextFile>> = MutableStateFlow(listOf())
    val textFiles: StateFlow<List<TextFile>> = _textFiles

    init {
        viewModelScope.launch {
            loadTextFilesFromInternalStorage()
        }
    }

    fun createTextFileInInternalStorage(fileName: String, content: String): Boolean {
        return try {
            val trimName = fileName.trim()
            val trimContent = content.trim()
            val fixedName = if (trimName.endsWith(".txt")) trimName else "$trimName.txt"
            context.openFileOutput(fixedName, AppCompatActivity.MODE_PRIVATE).use {
                it.write(trimContent.toByteArray())
            }
            /* TODO: Надо сделать наблюдатель за изменениями в файловой системе, чтобы самому вручную не обновлять список */
            loadTextFilesFromInternalStorage()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadTextFilesFromInternalStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            _textFiles.value =
                try {
                    val files = context.filesDir.listFiles()
                    files?.filter { it.canRead() and it.isFile and it.name.endsWith(".txt") }?.map {
                        TextFile(it.name)
                    } ?: listOf()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                    listOf()
                }
        }

    }

    fun getTextFileByName(filename: String) =
        context.openFileInput(filename).bufferedReader().use {
            TextFile(fileName = filename, content = it.readText())
        }


    fun deleteFileFromInternalStorage(filename: String): Boolean {
        val result = try {
            context.deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        /* TODO: Надо сделать наблюдатель за изменениями в файловой системе, чтобы самому вручную не обновлять список */
        if (result) loadTextFilesFromInternalStorage()
        return result
    }
}