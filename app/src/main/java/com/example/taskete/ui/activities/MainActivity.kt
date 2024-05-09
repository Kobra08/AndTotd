package com.example.taskete.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Group
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskete.R
import com.example.taskete.api.NetworkClient
import com.example.taskete.data.Task
import com.example.taskete.data.User
import com.example.taskete.data.UserResponse
import com.example.taskete.db.TasksDAO
import com.example.taskete.db.UsersDAO
import com.example.taskete.helpers.UIManager
import com.example.taskete.json.JSONFormatter
import com.example.taskete.preferences.PreferencesActivity
import com.example.taskete.preferences.SessionManager
import com.example.taskete.ui.adapter.RecyclerItemClickListener
import com.example.taskete.ui.adapter.TasksAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val TASK_SELECTED = "Task_selected"
const val LOGGED_USER = "LoggedUser"
private const val TAG_ACTIVITY = "MainActivity"
private const val PREFERENCE_ADDTASK = "swShowAddBtn"
private const val PREFERENCE_SHOWCOMPLETE = "swPrefShowCompletedTasks"

class MainActivity :
    AppCompatActivity(),
    RecyclerItemClickListener.OnItemClickListener,
    TaskSelection,
    EditDialogListener {

    private lateinit var rvTasks: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navDrawer: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var iconNoTasks: ImageView
    private lateinit var selectedTasks: MutableList<Task>
    private lateinit var loadingCircle: CircularProgressIndicator
    private lateinit var loadingBar: LinearProgressIndicator
    private lateinit var tasksGroup: Group
    private lateinit var userGroup: Group
    private var userRetrieved: Boolean
    private var showAddFab: Boolean
    private var showCompletedTasks: Boolean
    private var tasks: ArrayList<Task>
    private var actionMode: ActionMode? = null
    private var isMultiSelect: Boolean = false
    private val compositeDisposable = CompositeDisposable()

    private val userId: Int by lazy {
        SessionManager.restoreLoggedUser()
    }

    private var firstTrialLogin: Boolean

    private val preferences: Single<SharedPreferences> by lazy {
        Single.fromCallable { PreferenceManager.getDefaultSharedPreferences(this) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    private val tasksDAO: TasksDAO by lazy {
        TasksDAO(this@MainActivity.applicationContext)
    }

    private val usersDAO: UsersDAO by lazy {
        UsersDAO(this@MainActivity.applicationContext)
    }

    private val tasksAdapter: TasksAdapter by lazy {
        TasksAdapter(this)
    }

    private var currentUser: User?

    init {
        tasks = arrayListOf()
        showAddFab = false
        showCompletedTasks = false
        userRetrieved = false
        currentUser = null
        firstTrialLogin = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()

    }

    private fun setupUI() {
        fabAddTask = findViewById(R.id.fabAddTask)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.adapter = tasksAdapter
        iconNoTasks = findViewById(R.id.iconNoTasks)

        drawerLayout = findViewById(R.id.drawer_layout_main)
        tasksGroup = findViewById(R.id.tasksGroup)
        userGroup = findViewById(R.id.userGroup)
        loadingBar = findViewById(R.id.loadingBar)
        loadingCircle = findViewById(R.id.loadingCircle)

        fabAddTask.setOnClickListener {
            launchTaskActivity(null)
        }

        setupToolbar()
        setupCAB()
        setupNavbar()

    }

    private fun setupNavbar() {
        navDrawer = findViewById(R.id.drawer_layout_main)
        navView = findViewById(R.id.drawer_nav_view)

        val toggle =
            ActionBarDrawerToggle(this, navDrawer, R.string.nav_open_desc, R.string.nav_close_desc)
        navDrawer.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> showLogoutDialog()
                R.id.nav_settings -> launchSettingsActivity()
                R.id.nav_change_username -> showEditUsernameDialog()
            }

            false
        }
    }

    private fun showEditUsernameDialog() {
        EditUserDialogFragment()
            .show(supportFragmentManager, EditUserDialogFragment.TAG)
    }

    override fun onPositiveClick(editText: EditText) {
        val username = editText.text.toString()

        if (username.isEmpty()) {
            UIManager.showMessage(this, getText(R.string.changeUsernameError).toString())
        } else {
            currentUser?.let { user ->
                user.username = username
                updateUser(user)
            }
        }

        closeDrawer()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = resources.getText(R.string.main_screen_title)
        supportActionBar?.setHomeAsUpIndicator(null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun updateUser(user: User) {
        usersDAO.updateUser(user).subscribe(object : SingleObserver<Int> {
            override fun onSubscribe(d: Disposable?) {
                compositeDisposable.add(d)
            }

            override fun onSuccess(t: Int?) {
                loadUserProfile()
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG_ACTIVITY, "Error updating user because ${e?.message}")
            }

        })
    }

    private fun getLoggedUser() {
        if (userId != SessionManager.DEFAULT_USER_ID) {
            usersDAO.getUser(userId)
                .subscribe(object : SingleObserver<User?> {
                    override fun onSubscribe(d: Disposable?) {
                        compositeDisposable.add(d)
                    }

                    override fun onSuccess(t: User?) {
                        currentUser = t
                        userRetrieved = true
                        loadUserProfile()
                    }

                    override fun onError(e: Throwable?) {
                        Log.d(TAG_ACTIVITY, "Error retrieving user: ${e?.message}")
                    }
                })
        } else {
            finish()
        }
    }

    private fun getUserTasksFromDB() {
        tasksDAO.getUserTasks(userId).subscribe(object : SingleObserver<List<Task>> {
            override fun onSubscribe(d: Disposable?) {
                compositeDisposable.add(d)
            }

            override fun onSuccess(t: List<Task>?) {
                Log.d(TAG_ACTIVITY, "Tasks from user $userId could be retrieved")
                tasks = ArrayList(t)
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG_ACTIVITY, "Tasks from user $userId couldn't be retrieved")
            }

        })
    }

    private fun loadLoggedAccount() {
        showScreenLoading()
        getLoggedUser()
        showSettingsAfter(1000)
    }

    private fun loadTrialAccount() {
        checkFirstLogin()
        showScreenLoading()
        getTrialUser()
    }

    private fun checkFirstLogin() {
        firstTrialLogin = intent.extras?.getBoolean(TRIAL_LOGIN, false) ?: false

        if (firstTrialLogin) {
            showDisclaimerDialog()
            intent?.removeExtra(TRIAL_LOGIN)
        }
    }

    private fun showSettingsAfter(milliseconds: Long) {
        Handler(mainLooper).postDelayed({
            hideScreenLoading()
            showTasksAndSettings()
        }, milliseconds)
    }

    private fun getUserTasks() {
        if (SessionManager.isTrialMode()) {
            getUserTasksFromAPI()
        } else {
            getUserTasksFromDB()
        }
    }

    private fun getUserTasksFromAPI() {
        tasks = currentUser?.tasks as ArrayList<Task>
    }

    private fun getTrialUser() {
        NetworkClient.usersApi.getTrialUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.d(TAG_ACTIVITY, "User could be fetched from API")
                currentUser = response.body()?.let { JSONFormatter.getUserFromResponse(it) }
                loadUserProfile()
                showSettingsAfter(0)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.d(TAG_ACTIVITY, "User could NOT be fetched from API because ${t.message}")
            }
        })
    }

    private fun loadUserProfile() {
        val navHeader = navView.getHeaderView(0)
        val txtUsername = navHeader.findViewById<TextView>(R.id.txtUsername)
        val txtUsermail = navHeader.findViewById<TextView>(R.id.txtUserMail)

        txtUsername.text = currentUser?.username
            ?: resources.getString(R.string.nav_header_default_username)
        txtUsermail.text = currentUser?.mail
            ?: resources.getString(R.string.nav_header_default_usermail)

    }

    private fun checkSettings() {
        preferences.subscribe(object : SingleObserver<SharedPreferences> {
            override fun onSubscribe(d: Disposable?) {
                compositeDisposable.add(d)
            }

            override fun onSuccess(t: SharedPreferences) {
                showAddFab = t.getBoolean(PREFERENCE_ADDTASK, true)
                showCompletedTasks = t.getBoolean(PREFERENCE_SHOWCOMPLETE, true)
            }

            override fun onError(e: Throwable?) {
                Log.d(TAG_ACTIVITY, "The general preferences couldn't be retrieved", e)
            }

        })
    }

    private fun closeDrawer() {
        navDrawer.closeDrawer(GravityCompat.START)
    }

    private fun openDrawer() {
        navDrawer.openDrawer(GravityCompat.START)
    }

    private fun showLogoutDialog() {
        closeDrawer()
        AlertDialog.Builder(this)
            .setTitle(R.string.logoutDialogTitle)
            .setMessage(R.string.logoutDialogDesc)
            .setPositiveButton(R.string.logoutDialogOK) { _, _ ->
                SessionManager.setTrialModeFlag(false)
                SessionManager.saveLoggedUser(null)
                finish()
            }
            .setNegativeButton(R.string.logoutDialogNO) { _, _ ->
                //Go back to Menu
            }
            .setCancelable(false)
            .show()
    }


    private fun showDisclaimerDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.disclaimerTitle)
            .setMessage(R.string.disclaimerDesc)
            .setPositiveButton(R.string.disclaimerOKDialog) { _, _ ->
                //Nothing
            }
            .setCancelable(false)
            .show()
    }

    private fun setupCAB() {
        rvTasks.addOnItemTouchListener(
            RecyclerItemClickListener(this, rvTasks, this)
        )
    }

    private fun handleFabAddVisibility() {
        if (showAddFab) {
            UIManager.show(fabAddTask)
        } else {
            UIManager.hide(fabAddTask)
        }
    }

    private fun handleShowCompletedTasks() {
        if (!showCompletedTasks) {
            tasks = getPendingTasks()
        }

        tasksAdapter.updateTasks(tasks)

        if (tasks.isEmpty()) {
            UIManager.hide(rvTasks)
            UIManager.show(iconNoTasks)
        } else {
            UIManager.hide(iconNoTasks)
            UIManager.show(rvTasks)
        }
    }

    private fun getPendingTasks() = tasks.filter { t ->
        !t.isDone
    } as ArrayList<Task>


    override fun onBackPressed() {
        if (navDrawer.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            showLogoutDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> openDrawer()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun launchTaskActivity(task: Task?) {
        Intent(this, TaskFormActivity::class.java).apply {
            putExtra(TASK_SELECTED, task)
            putExtra(LOGGED_USER, currentUser)
            startActivity(this)
        }
    }

    private fun launchSettingsActivity() {
        Intent(this, PreferencesActivity::class.java).apply {
            startActivity(this)
        }
    }

    override fun onItemTouch(view: View?, position: Int) {
        if (isMultiSelect) {
            multiSelect(position)
        }
    }

    override fun onItemLongPress(view: View?, position: Int) {
        if (!isMultiSelect) {
            enableSelection()
        }

        multiSelect(position)
    }


    override fun onItemDoubleTouch(view: View?, position: Int) {
        if (!isMultiSelect) {
            launchTaskActivity(tasks[position])
        }
    }


    override fun onItemCheck(view: View?, position: Int) {
        if (!showCompletedTasks) {
            refreshList()
        }
    }

    private fun refreshList() {
        getUserTasks()
        Handler(mainLooper).postDelayed({
            resetSelection()
        }, 500)
    }


    private fun multiSelect(position: Int) {
        val taskSelected = tasks[position]

        if (selectedTasks.contains(taskSelected)) {
            selectedTasks.remove(taskSelected)
        } else {
            if (actionMode != null) {
                selectedTasks.add(taskSelected)
            }
        }

        tasksAdapter.getSelectedTasks(selectedTasks)

        actionMode?.title = if (selectedTasks.isNotEmpty()) "${selectedTasks.size}" else ""

        if (selectedTasks.isEmpty()) {
            disableSelection()
        }
    }

    private fun enableSelection() {
        isMultiSelect = true
        selectedTasks = mutableListOf()
        actionMode = actionMode ?: toolbar.startActionMode(TaskSelectionMode(this))

        if (showAddFab)
            UIManager.hide(fabAddTask)
    }

    private fun disableSelection() {
        isMultiSelect = false
        actionMode?.finish()
        actionMode = null

        if (showAddFab)
            UIManager.show(fabAddTask)
    }

    private fun deleteSelectedTasks() {
        selectedTasks.forEach { t ->
            tasksDAO.deleteTask(t).subscribe()
        }

    }

    override fun resetSelection() {
        handleShowCompletedTasks()
        disableSelection()
    }

    override fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.deleteDialogTitle)
            .setMessage(R.string.deleteDialogDesc)
            .setPositiveButton(R.string.deleteDialogOK) { _, _ ->
                deleteSelectedTasks()
                UIManager.showMessage(this, buildDeleteMessage())
                refreshList()
            }
            .setNegativeButton(R.string.deleteDialogNO) { _, _ ->
                resetSelection()
            }
            .setCancelable(false)
            .show()
    }

    private fun buildDeleteMessage(): String {
        val numberOfItems = selectedTasks.size
        return resources.getQuantityString(
            R.plurals.numberOfDeletedTasks,
            numberOfItems,
            numberOfItems
        )
    }

    //SHOW/HIDE UI methods
    private fun hideAll() {
        UIManager.hide(userGroup)
        UIManager.hide(navView)
        UIManager.hide(loadingBar)
        UIManager.hide(loadingCircle)
    }

    private fun showScreenLoading() {
        hideAll()
        UIManager.show(loadingCircle)
    }

    private fun hideScreenLoading() {
        UIManager.show(toolbar)
        UIManager.show(navView)
        UIManager.hide(loadingCircle)
    }

    //Loading after updating tasks/settings
    private fun showPreferencesLoading() {
        UIManager.hide(tasksGroup)
        UIManager.show(loadingBar)
    }

    private fun hidePreferencesLoading() {
        loadSettings()
        UIManager.hide(loadingBar)
    }

    private fun loadSettings() {
        handleShowCompletedTasks()
        handleFabAddVisibility()
    }

    private fun showTasksAndSettings() {
        showPreferencesLoading()
        checkSettings()
        getUserTasks()

        Handler(mainLooper).postDelayed({
            hidePreferencesLoading()
        }, 1000)
    }


    override fun onResume() {
        if (SessionManager.isTrialMode()) {
            loadTrialAccount()
        } else {
            if (!userRetrieved) {
                loadLoggedAccount()
            } else {
                showTasksAndSettings()
            }
        }

        super.onResume()
    }

    override fun onPause() {
        closeDrawer()
        super.onPause()
    }

    override fun onStop() {
        disableSelection()
        compositeDisposable.clear()
        super.onStop()
    }


}
