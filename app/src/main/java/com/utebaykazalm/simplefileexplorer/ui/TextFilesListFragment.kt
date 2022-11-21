package com.utebaykazalm.simplefileexplorer.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.utebaykazalm.simplefileexplorer.databinding.FragmentTextFilesListBinding
import com.utebaykazalm.simplefileexplorer.utils.collectLatestFlowInStarted
import dagger.hilt.android.AndroidEntryPoint

const val TFLF = "TextFilesListFragment"

@AndroidEntryPoint
class TextFilesListFragment : Fragment() {

    private val viewModel: GeneralViewModel by activityViewModels()

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
        binding.fabCreateTextFile.setOnClickListener {
            findNavController().navigate(
                TextFilesListFragmentDirections.actionTextFilesListFragmentToCreateTextFileFragment(
                    ""
                )
            )
        }
        collectLatestFlowInStarted(viewModel.textFiles) {
            filesListAdapter.submitList(it)
        }
//        lifecycleScope.launch {
//            viewModel.textFiles.collect {
//                filesListAdapter.submitList(it)
//            }
//        }
        binding.fabUpdateFiles.setOnClickListener {
            viewModel.updateFilesInUI()
        }
    }

    private fun setupFilesRecyclerView() {
        filesListAdapter = TextFilesListAdapter({
            Log.d(TFLF, "${it.fileName} was clicked")
            //Здесь будет показан контент.
            findNavController().navigate(
                TextFilesListFragmentDirections.actionTextFilesListFragmentToTextFileFragment(
                    it.fileName
                )
            )
        }) {
            Log.d(TFLF, "${it.fileName} was long clicked")
            viewModel.deleteFile(it.fileName)
            Toast.makeText(context, "File was deleted", Toast.LENGTH_SHORT).show()
            true
        }
        binding.rvFiles.adapter = filesListAdapter
    }

//    override fun onStart() {
//        super.onStart()
//        viewModel.updateFilesInUI()
//    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}