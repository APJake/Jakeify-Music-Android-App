package com.apjake.jakifymusic.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apjake.jakifymusic.common.AppConstants
import com.apjake.jakifymusic.exoplayer.MusicService
import com.apjake.jakifymusic.exoplayer.MusicServiceConnection
import com.apjake.jakifymusic.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = _currentSongDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> = _currentPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition (){

        viewModelScope.launch {
            while (true){
                val position = playbackState.value?.currentPlaybackPosition
                if(currentPlayerPosition.value!=position){
                    _currentPlayerPosition.postValue(position?:0L)
                    _currentSongDuration.postValue(MusicService.currentSongDuration)
                }
                delay(AppConstants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }

    }

}