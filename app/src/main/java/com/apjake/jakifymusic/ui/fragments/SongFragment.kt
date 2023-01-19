package com.apjake.jakifymusic.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.apjake.jakifymusic.R
import com.apjake.jakifymusic.common.Status
import com.apjake.jakifymusic.common.msToMinuteSecondString
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.databinding.FragmentSongBinding
import com.apjake.jakifymusic.exoplayer.isPlaying
import com.apjake.jakifymusic.exoplayer.toSong
import com.apjake.jakifymusic.ui.viewmodels.MainViewModel
import com.apjake.jakifymusic.ui.viewmodels.SongViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SongFragment: Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel: SongViewModel by viewModels()
    private lateinit var binding: FragmentSongBinding

    private var currentPlayingSong: Song?= null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        subscribeToObservers()

        binding.ivPlayPause.setOnClickListener {
            currentPlayingSong?.let{
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurrentTimeToView(progress.toLong() )
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }

        })
        binding.ivSkipNext.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
        binding.ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateSongHeader(song: Song){
        binding.tvSongName.text = song.title
        binding.tvSongSubtitle.text = song.subtitle
        glide.load(song.imageUrl).into(binding.ivSongImage)
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let { result ->
                when(result.status){
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if(currentPlayingSong==null && songs.isNotEmpty()){
                                currentPlayingSong = songs[0]
                                updateSongHeader(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(viewLifecycleOwner){
            if(it==null) return@observe
            currentPlayingSong = it.toSong()
            updateSongHeader(currentPlayingSong!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if(playbackState?.isPlaying==true) R.drawable.ic_pause else R.drawable.ic_play
            )
            binding.seekBar.progress = it?.position?.toInt()?:0
        }
        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner){
            if(shouldUpdateSeekBar){
                binding.seekBar.progress = it.toInt()
                setCurrentTimeToView(it?:0)
            }
        }
        songViewModel.currentSongDuration.observe(viewLifecycleOwner){
            binding.seekBar.max = it.toInt()
            binding.tvSongDuration.text = (it?:0).msToMinuteSecondString()
        }
    }

    private fun setCurrentTimeToView(milliSeconds: Long) {
        binding.tvCurTime.text = milliSeconds.msToMinuteSecondString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongBinding.inflate(inflater, container, false)

        return binding.root
    }
}