package com.example.taskete.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.example.taskete.R
import com.example.taskete.data.Task
import com.example.taskete.data.User
import com.example.taskete.db.UsersDAO
import com.example.taskete.helpers.UIManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.sql.SQLException

private const val TAG_ACTIVITY = "RegisterFormActivity"

class RegisterFormActivity : AppCompatActivity() {
    private lateinit var etUsername: TextInputEditText
    private lateinit var etMail: TextInputEditText
    private lateinit var etPass: TextInputEditText
    private lateinit var etConfirmPass: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var newUser: User
    private lateinit var registeredUsers: List<User>
    private var compositeDisposable = CompositeDisposable()

    private val usersDAO: UsersDAO by lazy {
        UsersDAO(this@RegisterFormActivity.applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_form)
        setupUI()
    }

    private fun setupUI() {
        etUsername = findViewById(R.id.etRegisterUsername)
        etMail = findViewById(R.id.etRegisterMail)
        etPass = findViewById(R.id.etRegisterPass)
        etConfirmPass = findViewById(R.id.etRegisterConfirmPass)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            createAccount()
        }

        loadRegisteredUsers()
    }

    private fun loadRegisteredUsers() {
        usersDAO.getUsers().subscribe(object : SingleObserver<List<User>> {
            override fun onSubscribe(d: Disposable?) {
                compositeDisposable.add(d)
            }

            override fun onSuccess(t: List<User>) {
                registeredUsers = t
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG_ACTIVITY, "Error when retrieving users because $e")
            }

        })
    }

    private fun createAccount() {
        if (inputIsValid()) {
            registerUser()
        } else
            showValidationErrorMessage()
    }

    private fun registerUser() {
        newUser = User(
                null,
                getText(etUsername),
                getText(etMail),
                getText(etPass),
                null,
                arrayListOf<Task>()
        )

        try {
            addUser()
        } catch (e: SQLException) {
            showRegisterErrorMessage()
        }

    }

    private fun addUser() {
        usersDAO.addUser(newUser).subscribe(object : SingleObserver<Int> {
            override fun onSubscribe(d: Disposable) {
                compositeDisposable.add(d)
            }

            override fun onSuccess(t: Int) {
                showRegisterSuccessMessage()
            }

            override fun onError(e: Throwable) {
                showRegisterErrorMessage()
            }

        })
    }


    private fun showRegisterSuccessMessage() {
        UIManager.showMessage(this, resources.getText(R.string.registerSuccess) as String)

        Handler(mainLooper).postDelayed({
            finish()
        }, 1000)
    }

    private fun showRegisterErrorMessage() {
        UIManager.showMessage(this, resources.getText(R.string.registerError) as String)

        Handler(mainLooper).postDelayed({
            finish()
        }, 1000)
    }

    private fun showValidationErrorMessage() {
        UIManager.showMessage(this, resources.getText(R.string.registerFieldsErrors) as String)
    }

    private fun inputIsValid(): Boolean = usernameIsValid() and (mailIsValid()) and (passIsValid())

    private fun usernameIsValid(): Boolean {
        val username = getText(etUsername)

        return if (username.trim().isEmpty()) {
            etUsername.error = resources.getText(R.string.usernameEmptyError)
            false
        } else
            true
    }

    private fun mailIsValid(): Boolean {
        val mail = getText(etMail)
        val validMailRegex = Regex(VALID_REGEX_PATTERN)

        //Reset error field
        etMail.doAfterTextChanged {
            if (etMail.error != null) {
                etMail.error = null
            }
        }

        //Not empty
        if (mail.trim().isEmpty()) {
            etMail.error = resources.getText(R.string.mailEmptyError)
            return false
        }

        //Valid mail Regex
        if (!mail.contains(validMailRegex)) {
            etMail.error = resources.getText(R.string.mailRegexError)
            return false
        }

        //Check mail existence
        if(isMailRegistered(mail)){
            etMail.error = resources.getText(R.string.mailExistentError)
            return false
        }

        return true
    }

    private fun passIsValid(): Boolean {
        val pass = getText(etPass)
        val confirmPass = getText(etConfirmPass)

        //Reset password field
        etPass.doAfterTextChanged {
            if (etPass.error != null) {
                    etPass.error = null
                }
        }

        etConfirmPass.doAfterTextChanged {
            if (etConfirmPass.error != null) {
                etConfirmPass.error = null
            }
        }

        //Not empty (both)
        if (pass.trim().isEmpty()) {
            etPass.error = resources.getText(R.string.passwordEmptyError)
            return false
        }

        //Length must be > 4 (both)
        if (pass.length < 4) {
            etPass.error = resources.getText(R.string.passwordLengthError)
            return false
        }

        //Pass and confirm must match
        if (confirmPass != pass) {
            etConfirmPass.error = resources.getText(R.string.passwordMismatchError)
            return false
        }

        return true
    }

    private fun isMailRegistered(mail: String): Boolean {
        val userWithMail = registeredUsers.firstOrNull { user ->
            user.mail.equals(mail, ignoreCase = true)
        }

        return userWithMail != null
    }

    private fun getText(view: EditText): String = view.text.toString()

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}