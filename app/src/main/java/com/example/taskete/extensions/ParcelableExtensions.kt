package com.example.taskete.extensions

import android.os.Parcel
import com.example.taskete.data.Priority
import java.util.*

//Int
const val NULL_NUMERIC_FLAG = 0
const val NULL_STRING_FLAG = ""

fun Parcel.writeNullableInt(value: Int?) {
    if (value != null) {
        writeInt(value)
    } else {
        writeInt(NULL_NUMERIC_FLAG)
    }
}

fun Parcel.readNullableInt(): Int? {
    return if (readInt() == NULL_NUMERIC_FLAG) {
        null
    } else
        readInt()
}

//String
fun Parcel.readNonNullableString(): String {
    return readString() ?: NULL_STRING_FLAG
}

//Date
fun Parcel.writeDateAsMillis(value: Date?) {
    if (value != null) {
        writeLong(value.time)
    } else {
        writeLong(NULL_NUMERIC_FLAG.toLong())
    }

}

fun Parcel.readMillisAsDate(): Date? {
    return if (readLong() == NULL_NUMERIC_FLAG.toLong()) {
        null
    } else {
        Date(readLong())
    }
}

//Make it Enum generic!
fun Parcel.readIntAsEnum(): Priority {
    val lastPos = Priority.values().size - 1
    val value = readInt()

    return if (value < 0 || value > lastPos) {
        Priority.NOTASSIGNED
    } else {
        Priority.values()[value]
    }
}

fun Parcel.writeEnumAsInt(value: Priority) {
    writeInt(value.ordinal)
}

