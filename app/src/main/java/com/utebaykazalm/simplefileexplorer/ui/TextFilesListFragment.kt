package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.utebaykazalm.simplefileexplorer.MAIN_ACTIVITY
import com.utebaykazalm.simplefileexplorer.TextFile
import com.utebaykazalm.simplefileexplorer.TextFilesListAdapter
import com.utebaykazalm.simplefileexplorer.databinding.FragmentTextFilesListBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
const val TFLF = "TextFilesListFragment"
class TextFilesListFragment : Fragment() {

    private var _binding: FragmentTextFilesListBinding? = null
    private val binding: FragmentTextFilesListBinding get() = _binding!!

    private lateinit var filesListAdapter: TextFilesListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTextFilesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilesRecyclerView()
        // createTextFileInInternalStorage("Baaby.txt","Giorno Giovanna")
        loadTextFilesIntoRecyclerView()
        binding.btnCreateTextFile.setOnClickListener {

        }
    }

    private fun setupFilesRecyclerView() {
        filesListAdapter = TextFilesListAdapter({
            Log.d(TFLF, "${it.fileName} was clicked")
        }) {
            Log.d(TFLF, "${it.fileName} was long clicked")
            deleteFileFromInternalStorage(it.fileName)
            loadTextFilesIntoRecyclerView()
            Toast.makeText(context, "File was deleted", Toast.LENGTH_SHORT).show()
            true
        }
        binding.rvFiles.adapter = filesListAdapter
    }

    private fun loadTextFilesIntoRecyclerView() {
        lifecycleScope.launch {
            val textFiles = getTextFilesFromInternalStorage()
            filesListAdapter.submitList(textFiles)
        }
    }

    private suspend fun getTextFilesFromInternalStorage(): List<TextFile> = withContext(Dispatchers.IO) {
        try {
            val files = requireContext().filesDir.listFiles()
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
            requireContext().deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createTextFileInInternalStorage(fileName: String, content: String): Boolean {
        return try {
            val fixedName = if (fileName.endsWith(".txt")) fileName else "$fileName.txt"
            requireContext().openFileOutput(fixedName, AppCompatActivity.MODE_PRIVATE).use {
                it.write(content.toByteArray())
            }
            loadTextFilesIntoRecyclerView()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}