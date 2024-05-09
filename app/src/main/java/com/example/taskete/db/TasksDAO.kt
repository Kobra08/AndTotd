package com.example.taskete.db

import android.content.Context
import android.util.Log
import com.example.taskete.data.Task
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.QueryBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class TasksDAO(context: Context) {
    private var dao: Dao<Task, Int>

    init {
        val helper = OpenHelperManager.getHelper(context, DBHelper::class.java)
        dao = helper.getDao(Task::class.java)
    }

    fun getUserTasks(userId: Int): Single<List<Task>> {
        return Single.fromCallable {
            dao.queryBuilder()
                    .where()
                    .eq(Task.USER_COL, userId)
                    .query()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun getTasks(): Single<List<Task>> {
        return Single.fromCallable { dao.queryForAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getTask(taskId: Int): Single<Task?> {
        return Single.fromCallable { dao.queryForId(taskId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteTask(task: Task): Single<Int> {
        return Single.fromCallable { dao.delete(task) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun addTask(task: Task): Single<Int> {
        return Single.fromCallable { dao.create(task) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateTask(task: Task): Single<Int> {
        return Single.fromCallable { dao.update(task) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun executeCustomQueries(queries: List<String>): Single<Unit> {
        return Single.fromCallable {
            for (query in queries) {
                try {
                    dao.executeRaw(query)
                }
                catch (e: Exception){
                    Log.d("TasksDAO", "Error when processing queries: ${e.message}")
                    break
                }
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}