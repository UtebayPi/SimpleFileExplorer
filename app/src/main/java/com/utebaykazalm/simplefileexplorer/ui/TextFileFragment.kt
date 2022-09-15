package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.utebaykazalm.simplefileexplorer.databinding.FragmentTextFileBinding


//TODO: надо создать ViewModel
class TextFileFragment : Fragment() {
    private val viewModel: TextFileViewModel by activityViewModels()
    private val args: TextFileFragmentArgs by navArgs()
    private var _binding: FragmentTextFileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTextFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textFile = viewModel.getTextFileByName(args.filename)
        binding.tvTextFileContent.text = textFile.content
        binding.tvTextFileName.text = textFile.fileName
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}