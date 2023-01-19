package com.apjake.jakifymusic.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.apjake.jakifymusic.R
import com.apjake.jakifymusic.common.Status
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.databinding.ActivityMainBinding
import com.apjake.jakifymusic.exoplayer.isPlaying
import com.apjake.jakifymusic.exoplayer.toSong
import com.apjake.jakifymusic.ui.adapters.BaseSongAdapter
import com.apjake.jakifymusic.ui.adapters.SwipeSongAdapter
import com.apjake.jakifymusic.ui.viewmodels.MainViewModel
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by viewModels()

    private var currentPlayingSong: Song? = null
    private var playbackState: PlaybackStateCompat? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        subscribeToObservers()

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.itemAt(position))
                }else{
                    currentPlayingSong = swipeSongAdapter.itemAt(position)
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        swipeSongAdapter.setItemClickListener {
            navHostFragment.navController
                .navigate(R.id.globalActionToSongFragment)
        }

        // navigation change listener

        navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navHostFragment.navController
            .addOnDestinationChangedListener{_, destination, _ ->
                when(destination.id){
                    R.id.songFragment -> toggleBottomBarVisibility(false)
                    R.id.homeFragment ->toggleBottomBarVisibility(true)
                    else -> toggleBottomBarVisibility(true)
                }
            }
    }

    private fun toggleBottomBarVisibility(visible: Boolean){
        binding.llCurrentMusic.isVisible = visible
    }

    private fun switchViewPagerToCurrentSong(song: Song){
        val newItemIndex = swipeSongAdapter.indexOf(song)
        if(newItemIndex != -1){
            binding.vpSong.currentItem = newItemIndex
            currentPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let { result ->
                when(result.status){
                    Status.SUCCESS ->{
                        result.data?.let { songs ->
                            swipeSongAdapter.submitList(songs)
                            if(songs.isNotEmpty()){
                                glide.load((currentPlayingSong?:songs[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentPlayingSong?: return@observe)
                        }
                    }
                    Status.LOADING -> Unit
                    Status.ERROR -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(this){
            it?.let {
                currentPlayingSong = it.toSong()
                glide.load(currentPlayingSong?.imageUrl).into(binding.ivCurSongImage)
                switchViewPagerToCurrentSong(currentPlayingSong?: return@observe)
            }
        }
        mainViewModel.playbackState.observe(this){
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if(playbackState?.isPlaying==true) R.drawable.ic_pause_circle
                else R.drawable.ic_play_circle
            )
        }
        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    Status.ERROR -> Snackbar.make(
                        binding.flFragmentContainer,
                        result.error?:"An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}