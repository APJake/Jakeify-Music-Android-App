package com.apjake.jakifymusic.ui.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.apjake.jakifymusic.data.entities.Song

abstract class BaseSongAdapter(): ListAdapter<Song, BaseSongAdapter.SongViewHolder>(SongDiffCallback) {

    protected var onItemClickListener: ((Song)->Unit)? = null

    fun setItemClickListener(listener: (Song)->Unit){
        onItemClickListener = listener
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        bind(getItem(position), holder.binding)
    }

    abstract fun bind(song: Song, viewBinding: ViewBinding)

    inner class SongViewHolder(
        val binding: ViewBinding
    ): RecyclerView.ViewHolder(binding.root)

    private object SongDiffCallback: DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}