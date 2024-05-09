package com.example.taskete.data

import android.os.Build
import android.os.Parcel
import java.util.*
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.example.taskete.extensions.*
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.collections.HashMap


@DatabaseTable(tableName = "Tasks")
class Task(
        @DatabaseField(generatedId = true)
        var id: Int? = null,
        @DatabaseField
        var title: String,
        @DatabaseField
        var description: String,
        @DatabaseField
        var priority: Priority,
        @DatabaseField
        var isDone: Boolean,
        @DatabaseField
        var dueDate: Date?,
        @DatabaseField(columnName = USER_COL, foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true, canBeNull = false)
        var user: User?
) : Parcelable {

    //Default constructor that ORMLite needs
    constructor() : this(null, "", "", Priority.NOTASSIGNED, false, null, null)

    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as Int?,
            parcel.readString() as String,
            parcel.readString() as String,
            parcel.readValue(Priority::class.java.classLoader) as Priority,
            parcel.readBoolean(),
            parcel.readValue(Date::class.java.classLoader) as? Date,
            null
    ) {
        user = _parentsUser.remove(id)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeValue(priority)
        parcel.writeBoolean(isDone)
        parcel.writeValue(dueDate)
        user?.let { _parentsUser.put(this.id ?: -1, it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        const val USER_COL: String = "userId"
        private var _parentsUser = HashMap<Int, User>()

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}


enum class Priority {
    NOTASSIGNED,
    LOW,
    MEDIUM,
    HIGH
}


@JsonClass(generateAdapter = true)
class TaskResponse(
    @Json(name="taskId")
    var id: Int? = null,
    var title: String,
    var description: String,
    var priority: Priority,
    var isDone: Boolean,
    var dueDate: String?,
    var userId: Int
)

