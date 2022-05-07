package com.apjake.jakifymusic.common

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val error: String?
){
    companion object{
        fun <T> success(data: T?) = Resource<T>(data = data, status = Status.SUCCESS, error = null)
        fun <T> error(message: String?, data: T? = null) = Resource<T>(data = data, status = Status.ERROR, error = message)
        fun <T> loading(data: T?=null) = Resource<T>(data = null, status = Status.LOADING, error = null)
    }
}

enum class Status{
    SUCCESS,
    ERROR,
    LOADING
}