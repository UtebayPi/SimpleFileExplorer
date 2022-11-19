package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.utebaykazalm.simplefileexplorer.data.TextFile
import com.utebaykazalm.simplefileexplorer.databinding.FragmentCreateEditTextFileBinding
import com.utebaykazalm.simplefileexplorer.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

const val CREATE_TFF = "CreateTextFileFragment"

@AndroidEntryPoint
class CreateEditTextFileFragment : Fragment() {

    private val args: CreateEditTextFileFragmentArgs by navArgs()

    private val viewModel: TextFileViewModel by activityViewModels()

    private var _binding: FragmentCreateEditTextFileBinding? = null
    private val binding: FragmentCreateEditTextFileBinding get() = _binding!!
    private var isEdit: Boolean = false
    private var oldFileName: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEditTextFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        val existingFileName = args.filename
        if (existingFileName.isNotBlank()) {
            val resultFile = viewModel.getTextFileByName(existingFileName)
            if (resultFile is Resource.Success) {
                isEdit = true
                binding.etTextFileName.setText(resultFile.data.fileName)
                binding.etTextFileContent.setText(resultFile.data.content)
            }
        }
        binding.btnSave.setOnClickListener {
            val filename = binding.etTextFileName.text.toString()
            val content = binding.etTextFileContent.text.toString()
            val textFile = TextFile(filename, content)
            val result = if (isEdit) {
                viewModel.editFileInIS(textFile, existingFileName)
            } else {
                viewModel.createFileInIS(textFile)
            }
            when (result) {
                is Resource.Success -> {
                    findNavController().navigate(
                        CreateEditTextFileFragmentDirections
                            .actionCreateTextFileFragmentToTextFileFragment(result.data.fileName)
                    )
                }
                is Resource.Error -> {
                    Snackbar.make(view, result.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateFilesInUI()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}