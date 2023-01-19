package com.apjake.jakifymusic.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.media.browse.MediaBrowser
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import com.apjake.jakifymusic.common.AppConstants
import com.apjake.jakifymusic.common.AppConstants.EVENT_NETWORK_ERROR
import com.apjake.jakifymusic.common.AppConstants.MEDIA_ROOT_ID
import com.apjake.jakifymusic.exoplayer.callbacks.MusicPlaybackPreparer
import com.apjake.jakifymusic.exoplayer.callbacks.MusicPlayerEventListener
import com.apjake.jakifymusic.exoplayer.callbacks.MusicPlayerNotificationListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private val serviceJob = Job()
    // custom service scope by matching main and service job
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private var currentPlayingSong: MediaMetadataCompat?  = null
    private var isPlayerInitialized = false

    var isForegroundService = false

    companion object{
        var currentSongDuration = 0L
            private set
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ){
            // when current song switches
            currentSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            // after prepared
            currentPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ){
        val currentSongIndex = if(currentPlayingSong==null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot? {
        // for later playlists (media root id is a kind of playlist id)
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID ->{
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if(!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    }else{
                        mediaSession.sendSessionEvent(EVENT_NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if(!resultSent){
                    result.detach()
                }
            }
        }
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

}