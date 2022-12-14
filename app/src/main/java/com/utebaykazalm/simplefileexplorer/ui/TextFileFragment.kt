package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.utebaykazalm.simplefileexplorer.databinding.FragmentTextFileBinding
import com.utebaykazalm.simplefileexplorer.utils.Resource


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
        val resultFile = viewModel.getTextFileByName(args.filename)
        if (resultFile !is Resource.Success) {
            findNavController().popBackStack()
            return
        }
        binding.tvTextFileContent.text = resultFile.data.content
        val filename = resultFile.data.fileName
        binding.tvTextFileName.text = filename
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                TextFileFragmentDirections.actionTextFileFragmentToCreateTextFileFragment(
                    filename
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}