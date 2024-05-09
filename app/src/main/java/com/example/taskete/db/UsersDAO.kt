package com.example.taskete.db

import android.content.Context
import com.example.taskete.data.Task
import com.example.taskete.data.User
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class UsersDAO(context: Context) {
    private var dao: Dao<User, Int>

    init {
        val helper = OpenHelperManager.getHelper(context, DBHelper::class.java)
        dao = helper.getDao(User::class.java)
    }

    fun getUsers(): Single<List<User>> {
        return Single.fromCallable { dao.queryForAll() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getUser(userId: Int): Single<User?> {
        return Single.fromCallable { dao.queryForId(userId) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteUser(user: User): Single<Int> {
        return Single.fromCallable { dao.delete(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun addUser(user: User): Single<Int> {
        return Single.fromCallable { dao.create(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun updateUser(user: User): Single<Int> {
        return Single.fromCallable { dao.update(user) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}