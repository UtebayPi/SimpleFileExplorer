package com.utebaykazalm.simplefileexplorer.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utebaykazalm.simplefileexplorer.data.TextFile
import com.utebaykazalm.simplefileexplorer.utils.Resource
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

    fun createOrEditTextFileInInternalStorage(
        fileName: String,
        content: String,
        isEdit: Boolean,
        oldFileName: String = ""
    ): Resource<TextFile> {
        try {
            //TODO: Отдельный один метод для форматирования и верификаций
            val trimName = fileName.trim()
            val trimContent = content.trim()
            if (trimName.isBlank() or trimContent.isBlank()) return Resource.Error("Content or name is empty")
            val fixedName = when {
                trimName.endsWith(".txt") -> trimName
                trimName.contains(".") -> return Resource.Error("Name contains \".\" symbol")
                else -> "$trimName.txt"
            }
            if (!isEdit) {
                if (context.fileList().map { it.lowercase() }
                        .contains(fixedName.lowercase())) {
                    return Resource.Error("That filename is already used")
                }
            } else if (oldFileName.isNotBlank()) {
                deleteFileFromInternalStorage(oldFileName)
            }
            val textFile = TextFile(fixedName, trimContent)
            //TODO: Отдельный метод для сохранения TextFile в памяти. Чтобы не было возможности заменить так.
            // Чтобы заменить, нужно сперва удалить. После удаления, снова проверить из списка имен.
            context.openFileOutput(textFile.fileName, AppCompatActivity.MODE_PRIVATE).use {
                it.write(textFile.content.toByteArray())
            }
            /* TODO: Надо сделать наблюдатель за изменениями в файловой системе, чтобы самому вручную не обновлять список */
            loadTextFilesFromInternalStorage()
            return Resource.Success(textFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Exception error: ${e.message}")
        }
    }

    private fun loadTextFilesFromInternalStorage() {
        viewModelScope.launch(Dispatchers.IO) {
            _textFiles.value = try {
                val files = context.filesDir.listFiles()
                //and it.name.endsWith(".txt")
                files?.filter { it.canRead() and it.isFile }?.map {
                    TextFile(it.name)
                } ?: listOf()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                listOf()
            }
        }

    }

    fun getTextFileByName(filename: String) = context.openFileInput(filename).bufferedReader().use {
        TextFile(filename, it.readText())
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