package com.apjake.jakifymusic.common

open class Event<out T>(private val data: T) {

    var hasBeenHandle = false
        private set

    fun getContentIfNotHandled(): T?{
        return if(hasBeenHandle){
            null
        } else {
            hasBeenHandle = false
            data
        }
    }

    fun peekContent() = data

}