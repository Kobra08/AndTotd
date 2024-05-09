package com.example.taskete.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val formatter = SimpleDateFormat("d MMM yyyy HH:mm")

fun Date.stringFromDate(): String {
    return formatter.format(this)
}

fun String.dateFromString(): Date? {
    return formatter.parse(this)
}

