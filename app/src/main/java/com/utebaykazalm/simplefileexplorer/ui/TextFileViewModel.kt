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

    //разделить на create и update
    fun createOrEditTextFileInInternalStorage(
        givenTextFile: TextFile, isEdit: Boolean, oldFileName: String = ""
    ): Resource<TextFile> {
        try {
            //TODO: Отдельный один метод для форматирования и верификаций
            val resultFile = givenTextFile.trimVerifyCreate()
            if (resultFile is Resource.Error) return resultFile
            val textFile = resultFile.data!!
            val availableNames = context.fileList().map { it.lowercase() } as MutableList
            if (isEdit and oldFileName.isNotBlank()) {
                availableNames.remove(oldFileName.lowercase())
            }
            if (availableNames.contains(textFile.fileName.lowercase())) {
                return Resource.Error("That filename is already used")
            }
            //TODO: Отдельный метод для сохранения TextFile в памяти. Чтобы не было возможности заменить так.
            // Чтобы заменить, нужно сперва удалить. После удаления, снова проверить из списка имен.
            context.openFileOutput(textFile.fileName, AppCompatActivity.MODE_PRIVATE).use {
                it.write(textFile.content.toByteArray())
            }
            if (isEdit and (textFile.fileName.lowercase() != oldFileName.lowercase())) deleteFileFromInternalStorage(oldFileName)
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
                    TextFile(it.name,"")
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