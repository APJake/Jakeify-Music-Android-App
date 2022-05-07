package com.apjake.jakifymusic.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apjake.jakifymusic.common.AppConstants
import com.apjake.jakifymusic.common.AppConstants.MEDIA_ROOT_ID
import com.apjake.jakifymusic.common.Resource
import com.apjake.jakifymusic.data.entities.Song
import com.apjake.jakifymusic.exoplayer.MusicServiceConnection
import com.apjake.jakifymusic.exoplayer.isPlayEnabled
import com.apjake.jakifymusic.exoplayer.isPlaying
import com.apjake.jakifymusic.exoplayer.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnection = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading())
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        mediaId = it.mediaId?:"",
                        title = it.description.title.toString(),
                        subtitle = it.description.subtitle.toString(),
                        singer = it.description.mediaDescription.toString(),
                        songUrl = it.description.mediaUri.toString(),
                        imageUrl = it.description.iconUri.toString(),
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong() = musicServiceConnection.transportControl.skipToNext()
    fun skipToPreviousSong() = musicServiceConnection.transportControl.skipToPrevious()
    fun seekTo(position: Long) = musicServiceConnection.transportControl.seekTo(position)

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false){
        val isPrepared = playbackState.value?.isPrepared?: false
        if(isPrepared &&
            mediaItem.mediaId == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let {playbackState ->
                when{
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControl.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControl.play()
                    else-> Unit
                }
            }
        }else{
            musicServiceConnection.transportControl.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}