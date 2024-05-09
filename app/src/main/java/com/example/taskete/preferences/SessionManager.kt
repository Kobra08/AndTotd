package com.example.taskete.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.taskete.data.User
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SessionManager {
    companion object {
        const val DEFAULT_USER_ID = -1
        const val SESSION_PREFERENCES = "SessionPreferences"
        const val LOGGED_USER_ID = "LoggedUserId"
        const val TRIAL_MODE ="Trial"
        private const val FIRST_LOGIN = "FirstLogin"
        private val compositeDisposable = CompositeDisposable()
        private lateinit var sharedPreferences: SharedPreferences

        fun getPreferences(context: Context) {
            Single.fromCallable {
                context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<SharedPreferences> {
                        override fun onSubscribe(d: Disposable?) {
                            Log.d(SessionManager::class.java.name, "Waiting for shared preferences")
                            compositeDisposable.add(d)
                        }

                        override fun onSuccess(t: SharedPreferences) {
                            Log.d(SessionManager::class.java.name, "Shared Preferences OK!")
                            sharedPreferences = t
                        }

                        override fun onError(e: Throwable?) {
                            Log.d(SessionManager::class.java.name, "Couldn't retrieve Shared Preferences because $e")
                            compositeDisposable.clear()
                        }
                    })
        }

        fun restoreLoggedUser(): Int {
            return sharedPreferences.getInt(LOGGED_USER_ID, DEFAULT_USER_ID)
        }

        fun saveLoggedUser(userId: Int?) {
            sharedPreferences.edit {
                putInt(LOGGED_USER_ID, userId ?: DEFAULT_USER_ID)
                putBoolean(FIRST_LOGIN, true)
                apply()
            }
        }

        fun setTrialModeFlag(value: Boolean){
            sharedPreferences.edit{
                putBoolean(TRIAL_MODE, value)
                apply()
            }
        }

        fun isTrialMode() = sharedPreferences.getBoolean(TRIAL_MODE, false)
    }
}

