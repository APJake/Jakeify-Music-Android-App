package com.apjake.jakifymusic.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.databinding.SinglePlayingSongItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class SwipeSongAdapter: BaseSongAdapter() {

    fun indexOf(song: Song): Int =currentList.indexOf(song)

    fun itemAt(pos: Int): Song = getItem(pos)

    override fun bind(song: Song, viewBinding: ViewBinding) {
        val binding = viewBinding as SinglePlayingSongItemBinding
        binding.tvTitle.text = song.title
        binding.tvDescription.text = song.subtitle

        binding.clHolder.setOnClickListener {
            onItemClickListener?.let { it(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            SinglePlayingSongItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}