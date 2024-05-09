package com.example.taskete.helpers

import android.os.Parcel
import android.os.Parcelable

object ParcelableUtil {
    fun marshall(parcelable: Parcelable): ByteArray? {
        val parcelOut = Parcel.obtain()
        parcelable.writeToParcel(parcelOut, 0)
        val bytes = parcelOut.marshall()
        parcelOut.recycle()
        return bytes
    }

    fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0) // This is extremely important!
        return parcel
    }

    fun <T> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = unmarshall(bytes)
        val result = creator.createFromParcel(parcel)
        parcel.recycle()
        return result
    }

}