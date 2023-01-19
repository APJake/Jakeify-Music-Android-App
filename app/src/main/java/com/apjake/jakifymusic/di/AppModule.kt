package com.apjake.jakifymusic.di

import android.content.Context
import com.apjake.jakifymusic.R
import com.apjake.jakifymusic.exoplayer.MusicServiceConnection
import com.apjake.jakifymusic.ui.adapters.SongAdapter
import com.apjake.jakifymusic.ui.adapters.SwipeSongAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.music_icon)
            .error(R.drawable.error_thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Provides
    @Singleton
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context,
    ) = MusicServiceConnection(context)

    @Provides
    @Singleton
    fun provideSwipeSongAdapter() = SwipeSongAdapter()


}