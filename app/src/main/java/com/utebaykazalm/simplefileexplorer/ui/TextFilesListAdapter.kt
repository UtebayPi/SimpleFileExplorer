package com.utebaykazalm.simplefileexplorer.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utebaykazalm.simplefileexplorer.data.TextFile
import com.utebaykazalm.simplefileexplorer.databinding.ItemTextFileBinding

class TextFilesListAdapter(private val onClick: (TextFile) -> Unit, private val onLongClick: (TextFile) -> Boolean) :
    ListAdapter<TextFile, TextFilesListAdapter.TextFileViewHolder>(DiffCallback) {
    class TextFileViewHolder(private var binding: ItemTextFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(textFile: TextFile) {
            binding.tvFileName.text = textFile.fileName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextFileViewHolder {
        return TextFileViewHolder(ItemTextFileBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TextFileViewHolder, position: Int) {
        val textFile = getItem(position)
        holder.bind(textFile)
        holder.itemView.setOnClickListener {
            onClick(textFile)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(textFile)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TextFile>() {
        override fun areItemsTheSame(oldItem: TextFile, newItem: TextFile): Boolean {
            return oldItem.fileName == newItem.fileName
        }

        override fun areContentsTheSame(oldItem: TextFile, newItem: TextFile): Boolean {
            return oldItem == newItem
        }
    }
}