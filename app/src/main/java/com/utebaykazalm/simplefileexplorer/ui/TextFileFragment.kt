package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.utebaykazalm.simplefileexplorer.data.TextFile
import com.utebaykazalm.simplefileexplorer.databinding.FragmentTextFileBinding
import java.io.File


//TODO: надо создать ViewModel
class TextFileFragment : Fragment() {
    private val args: TextFileFragmentArgs by navArgs()
    private var _binding: FragmentTextFileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTextFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filename = args.filename
        getTextFileByName(filename)
    }

    //TODO: ЭТо точно не в фрагменте должно быть. Потом переместить в репозиторий.
    private fun getTextFileByName(filename: String) {
        context?.openFileInput(filename)?.bufferedReader()?.use {
            binding.tvTextFileContent.text = it.readText()
            binding.tvTextFileName.text = filename
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}