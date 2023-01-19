package com.apjake.jakifymusic.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.apjake.jakifymusic.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? = this.description?.let {
        Song(
            mediaId = it.mediaId.toString(),
            title = it.title.toString(),
            subtitle = it.subtitle.toString(),
            songUrl = it.mediaUri.toString(),
            imageUrl = it.iconUri.toString()
        )
    }
