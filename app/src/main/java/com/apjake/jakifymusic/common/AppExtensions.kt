package com.apjake.jakifymusic.common

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor

fun Long.msToMinuteSecondString(): String {
    val minutes = floor(this/1000/60.0).toInt()
    val seconds = ((this/1000.0)%60).toInt()
    return "${String.format("%02d",minutes)}:${String.format("%02d",seconds)}"
}

