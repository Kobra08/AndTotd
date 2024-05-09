package com.example.taskete.data

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.taskete.extensions.*
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@DatabaseTable(tableName = "Users")
class User(
    @DatabaseField(generatedId = true)
    var id: Int? = null,
    @DatabaseField
    var username: String,
    @DatabaseField(unique = true)
    var mail: String,
    @DatabaseField
    var password: String,
    @DatabaseField
    var avatar: String? = null,
    @ForeignCollectionField(eager = false)
    var tasks: Collection<Task>
) : Parcelable {

    constructor() : this(null, "", "", "", null, arrayListOf<Task>())

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as Int?,
        parcel.readString() as String,
        parcel.readString() as String,
        parcel.readString() as String,
        parcel.readString(),
        arrayListOf<Task>().apply {
            parcel.readParcelableList(this, Task::class.java.classLoader)
        }
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(username)
        parcel.writeString(mail)
        parcel.writeString(password)
        parcel.writeString(avatar)

        arrayListOf<Task>().also {
            for (task in tasks) {
                it.add(task)
            }
            parcel.writeParcelableList(it, flags)
        }
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}

@JsonClass(generateAdapter = true)
class UserResponse(
    @Json(name = "userId")
    var id: Int?,
    var username: String,
    var mail: String,
    var password: String,
    var avatar: String? = null,
    var tasks: List<TaskResponse>
)


