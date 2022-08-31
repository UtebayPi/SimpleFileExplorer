package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.utebaykazalm.simplefileexplorer.databinding.FragmentCreateTextFileBinding

class CreateTextFileFragment : Fragment() {

    private var _binding: FragmentCreateTextFileBinding? = null
    private val binding: FragmentCreateTextFileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateTextFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnSave.setOnClickListener {
            val filename = binding.etTextFileName.text.toString()
            val content = binding.tvTextFileContent.text.toString()
            if (filename.isEmpty() or content.isEmpty()) return@setOnClickListener
            createTextFileInInternalStorage(filename, content)
            findNavController().popBackStack()
        }
    }

    private fun createTextFileInInternalStorage(fileName: String, content: String): Boolean {
        return try {
            val trimName = fileName.trim()
            val trimContent = content.trim()
            val fixedName = if (trimName.endsWith(".txt")) trimName else "$trimName.txt"
            context?.openFileOutput(fixedName, AppCompatActivity.MODE_PRIVATE).use {
                it?.write(trimContent.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}