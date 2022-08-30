package com.utebaykazalm.simplefileexplorer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.utebaykazalm.simplefileexplorer.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

const val MAIN_ACTIVITY = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var filesListAdapter: TextFilesListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFilesRecyclerView()
        loadTextFilesIntoRecyclerView()
        binding.btnCreateTextFile.setOnClickListener{

        }
    }

    private fun setupFilesRecyclerView() {
        filesListAdapter = TextFilesListAdapter({
            Log.d(MAIN_ACTIVITY, "${it.fileName} was clicked")
        }) {
            Log.d(MAIN_ACTIVITY, "${it.fileName} was long clicked")
            deleteFileFromInternalStorage(it.fileName)
            loadTextFilesIntoRecyclerView()
            true
        }
        binding.rvFiles.adapter = filesListAdapter
    }

    private fun loadTextFilesIntoRecyclerView() {
        val testTextFiles = listOf(
            TextFile("a"),
            TextFile("b"),
            TextFile("c"),
            TextFile("d"),
            TextFile("e"),
            TextFile("f"),
        )
        lifecycleScope.launch {
            val textFiles = getTextFilesFromInternalStorage()
            filesListAdapter.submitList(textFiles)
        }
    }

    private suspend fun getTextFilesFromInternalStorage(): List<TextFile> = withContext(Dispatchers.IO) {
        try {
            val files = filesDir.listFiles()
            files?.filter { it.canRead() and it.isFile and it.name.endsWith(".txt") }?.map {
                TextFile(it.name)
            } ?: listOf()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            listOf()
        }

    }

    private fun deleteFileFromInternalStorage(filename: String): Boolean {
        return try {
            deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createTextFileInInternalStorage(fileName: String, content: String = ""): Boolean {
        return try {
            val fixedName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt"
            openFileOutput(fixedName, MODE_PRIVATE).use {
                it.write(content.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}