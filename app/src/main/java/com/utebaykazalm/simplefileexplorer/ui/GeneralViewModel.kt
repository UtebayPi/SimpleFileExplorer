package com.utebaykazalm.simplefileexplorer.ui

import kotlinx.coroutines.flow.asStateFlow
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GeneralViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {

    val GVM = "GeneralViewModel"

    private var _textFiles: MutableStateFlow<List<TextFile>> = MutableStateFlow(listOf())
    val textFiles: StateFlow<List<TextFile>> = _textFiles

    private var _textFile: MutableStateFlow<TextFile> = MutableStateFlow(TextFile("", ""))
    val textFile = _textFile.asStateFlow()

    init {
        viewModelScope.launch {
            updateFilesInUI()
        }
    }

    suspend fun getFileByName(filename: String): Resource<TextFile> {
        return withContext(Dispatchers.IO) {
            Log.d(GVM, "getFileByName() was called!")
            try {
                if (textFile.value.fileName == filename) return@withContext Resource.Success(textFile.value)
                //val external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                //работало в эмуле, но не в телефоне. оно depricated. возможно из за этого. Или ей нужно разрешение какое то.
                //а пока возвращяемся к этому.
                Log.d(GVM, "getExternalFilesDir() in getFileByName() was called!")
                val external = context.getExternalFilesDir(null)
                val file = File(external, filename)
                val newTextFile = file.inputStream().bufferedReader().use {
                    TextFile(filename, it.readText())
                }
                _textFile.value = newTextFile
                Resource.Success(newTextFile)
            } catch (e: Exception) {
                Toast.makeText(context, "Exception: " + e.message.toString(), Toast.LENGTH_SHORT).show()
                Resource.Error("Exception: " + e.message.toString())
            }
        }
    }

    //TODO: Сделать асинхронным
    private suspend fun getFileNames() = withContext(Dispatchers.IO) {
        Log.d(GVM, "getFileNames() was called!")
        context.getExternalFilesDir(null)?.listFiles()
            ?.map { it.name.lowercase() }
    }

    suspend fun createFile(
        initTextFile: TextFile
    ): Resource<TextFile> {
        Log.d(GVM, "createFileInIS() was called!")
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

    suspend fun editFile(
        initTextFile: TextFile, oldName: String
    ): Resource<TextFile> {
        Log.d(GVM, "editFileInIS() was called!")
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
                deleteFile(oldName)
            return Resource.Success(textFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error("Exception error: ${e.message}")
        }
    }

    private suspend fun saveFile(textFile: TextFile) = withContext(Dispatchers.IO) {
        Log.d(GVM, "saveFile() was called!")
        try {
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


    fun deleteFile(filename: String) = viewModelScope.launch(Dispatchers.IO) {
        Log.d(GVM, "deleteFileFromIS() was called!")
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


    /* TODO: Надо сделать наблюдатель за изменениями в файловой системе, чтобы самому вручную не обновлять список */
    fun updateFilesInUI() = viewModelScope.launch(Dispatchers.IO) {
        Log.d(GVM, "updateFilesInUI() was called!")
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