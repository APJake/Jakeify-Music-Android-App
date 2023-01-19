package com.apjake.jakifymusic.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.databinding.SingleSongItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
): BaseSongAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            SingleSongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun bind(song: Song, viewBinding: ViewBinding) {
        val binding = viewBinding as SingleSongItemBinding
        binding.tvTitle.text = song.title
        binding.tvDescription.text = song.subtitle
        glide.load(song.imageUrl).into(binding.ivItemImage)
        binding.clHolder.setOnClickListener {
            onItemClickListener?.let { it(song) }
        }
    }


}