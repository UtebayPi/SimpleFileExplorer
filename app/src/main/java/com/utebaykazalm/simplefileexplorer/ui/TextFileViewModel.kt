package com.utebaykazalm.simplefileexplorer.ui

import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import android.widget.Toast
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
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TextFileViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {

    val TFVM = "TextFileViewModel"

    private var _textFiles: MutableStateFlow<List<TextFile>> = MutableStateFlow(listOf())
    val textFiles: StateFlow<List<TextFile>> = _textFiles

    private var _textFile: MutableStateFlow<TextFile> = MutableStateFlow(TextFile("", ""))
    val textFile = _textFile.asStateFlow()

//    init {
//        viewModelScope.launch {
//            updateFilesInUI()
//        }
//    }

    fun getTextFileByName(filename: String): Resource<TextFile> {
        return try {
            //val external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            //работало в эмуле, но не в телефоне. оно depricated. возможно из за этого. Или ей нужно разрешение какое то.
            //а пока возвращяемся к этому.
            //TODO: Сделать асинхронным
            val external = context.getExternalFilesDir(null)
            val file = File(external, filename)
            val textFile = file.inputStream().bufferedReader().use {
                TextFile(filename, it.readText())
            }
//            val textFile = context.openFileInput(filename).bufferedReader().use {
//                TextFile(filename, it.readText())
//            }
            Resource.Success(textFile)
        } catch (e: Exception) {
            Toast.makeText(context, "Exception: " + e.message.toString(), Toast.LENGTH_SHORT).show()
            Resource.Error("Exception: " + e.message.toString())
        }
    }

    //TODO: Сделать асинхронным
    private fun getFileNames() =
        context.getExternalFilesDir(null)?.listFiles()
            ?.map { it.name.lowercase() }

    fun createFileInIS(
        initTextFile: TextFile
    ): Resource<TextFile> {
        try {
            val resultFile = initTextFile.trimVerifyCreate()
            if (resultFile is Resource.Error) return resultFile
            val textFile = resultFile.data!!
            if (getFileNames()?.contains(textFile.fileName.lowercase()) == true)
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
            val availableNames = getFileNames()?.toMutableList()
            availableNames?.remove(oldName.lowercase())
            if (availableNames?.contains(textFile.fileName.lowercase()) == true)
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

    //TODO: Сделать асинхронным
    private fun saveFile(textFile: TextFile): Boolean {
        return try {
            val external = context.getExternalFilesDir(null)
            val file = File(external, textFile.fileName)
            file.outputStream().use {
                it.write(textFile.content.toByteArray())
            }
//            context.openFileOutput(textFile.fileName, AppCompatActivity.MODE_PRIVATE).use {
//                it.write(textFile.content.toByteArray())
//            }
            updateFilesInUI()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            false
        }
    }

    fun deleteFileFromIS(filename: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                val files = context.getExternalFilesDir(null)?.listFiles()
                files?.find { it.name == filename }?.delete()
                true
                //context.deleteFile(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            if (result) updateFilesInUI()
        }
    }

    /* TODO: Надо сделать наблюдатель за изменениями в файловой системе, чтобы самому вручную не обновлять список */
    fun updateFilesInUI() {
        viewModelScope.launch(Dispatchers.IO) {
            _textFiles.value = try {
                val files = context.getExternalFilesDir(null)?.listFiles()
                //val files = context.getExternalFilesDir(null)?.listFiles()
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