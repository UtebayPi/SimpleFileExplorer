package com.utebaykazalm.simplefileexplorer.ui

import android.content.Context
import android.widget.Toast
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
            updateFilesInUI()
        }
    }

    fun getTextFileByName(filename: String): Resource<TextFile> {
        return try {
            val textFile = context.openFileInput(filename).bufferedReader().use {
                TextFile(filename, it.readText())
            }
            Resource.Success(textFile)
        } catch (e: Exception) {
            Toast.makeText(context, "Exception: " + e.message.toString(), Toast.LENGTH_SHORT).show()
            Resource.Error("Exception: " + e.message.toString())
        }
    }

    private fun getFileNames() = context.fileList().map { it.lowercase() } as MutableList

    fun createFileInIS(
        initTextFile: TextFile
    ): Resource<TextFile> {
        try {
            val resultFile = initTextFile.trimVerifyCreate()
            if (resultFile is Resource.Error) return resultFile
            val textFile = resultFile.data!!
            if (getFileNames().contains(textFile.fileName.lowercase()))
                return Resource.Error("That filename is already used")
            saveFile(textFile)
            return Resource.Success(textFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Exception error: ${e.message}")
        }
    }

    fun editFileInIS(
        initTextFile: TextFile, oldName: String
    ): Resource<TextFile> {
        try {
            val resultFile = initTextFile.trimVerifyCreate()
            if (resultFile is Resource.Error) return resultFile
            val textFile = resultFile.data!!
            val availableNames = getFileNames()
            availableNames.remove(oldName.lowercase())
            if (availableNames.contains(textFile.fileName.lowercase()))
                return Resource.Error("That filename is already used")
            saveFile(textFile)
            if ((textFile.fileName.lowercase() != oldName.lowercase()))
                deleteFileFromIS(oldName)
            return Resource.Success(textFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Exception error: ${e.message}")
        }
    }

    private fun saveFile(textFile: TextFile): Boolean {
        return try {
            context.openFileOutput(textFile.fileName, AppCompatActivity.MODE_PRIVATE).use {
                it.write(textFile.content.toByteArray())
            }
            updateFilesInUI()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            false
        }
    }

    fun deleteFileFromIS(filename: String): Boolean {
        val result = try {
            context.deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        if (result) updateFilesInUI()
        return result
    }

    /* TODO: ???????? ?????????????? ?????????????????????? ???? ?????????????????????? ?? ???????????????? ??????????????, ?????????? ???????????? ?????????????? ???? ?????????????????? ???????????? */
    private fun updateFilesInUI() {
        viewModelScope.launch(Dispatchers.IO) {
            _textFiles.value = try {
                val files = context.filesDir.listFiles()
                //and it.name.endsWith(".txt")
                files?.filter { it.canRead() and it.isFile }?.map {
                    TextFile(it.name, "")
                } ?: listOf()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.printStackTrace()
                listOf()
            }
        }

    }
}