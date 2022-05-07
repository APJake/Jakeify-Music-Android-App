package com.apjake.jakifymusic.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.databinding.SingleSongItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
): ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback) {

    private var onItemClickListener: ((Song)->Unit)? = null

    fun setOnItemClickListener(listener: (Song)->Unit){
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            SingleSongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song: Song = getItem(position)
        holder.bind(song)
    }


    inner class SongViewHolder(
        private val binding: SingleSongItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(song: Song){
            binding.tvTitle.text = song.title
            binding.tvDescription.text = song.subtitle
            glide.load(song.imageUrl).into(binding.ivItemImage)
            binding.clHolder.setOnClickListener {
                onItemClickListener?.let { it(song) }
            }
        }
    }

    private object SongDiffCallback: DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

    }
}