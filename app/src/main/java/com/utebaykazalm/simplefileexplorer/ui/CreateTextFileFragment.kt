package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.utebaykazalm.simplefileexplorer.databinding.FragmentCreateTextFileBinding
import dagger.hilt.android.AndroidEntryPoint

const val CREATE_TFF = "CreateTextFileFragment"

@AndroidEntryPoint
class CreateTextFileFragment : Fragment() {

    private val viewModel: TextFileViewModel by activityViewModels()

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
            val content = binding.etTextFileContent.text.toString()
            if (filename.isEmpty() or content.isEmpty()) return@setOnClickListener
            viewModel.createTextFileInInternalStorage(filename, content)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}