package com.example.taskete.json

import com.example.taskete.data.Task
import com.example.taskete.data.TaskResponse
import com.example.taskete.data.User
import com.example.taskete.data.UserResponse
import com.example.taskete.extensions.dateFromString
import kotlin.math.log

object JSONFormatter {
    fun getUserFromResponse(response: UserResponse): User {
        val user = User()
        user.id = response.id
        user.username = response.username
        user.password = response.password
        user.mail = response.mail
        user.avatar = response.avatar
        user.tasks = arrayListOf<Task>().apply {
            for (taskResponse in response.tasks) {
                this.add(getTaskFromResponse(taskResponse))
            }
        }

        return user
    }

    fun getTaskFromResponse(response: TaskResponse): Task {
        val task = Task()
        task.id = response.id
        task.title = response.title
        task.description = response.description
        task.dueDate = response.dueDate?.dateFromString()
        task.priority = response.priority
        task.isDone = response.isDone
        task.user = User().also { it.id = response.userId }

        return task
    }

}