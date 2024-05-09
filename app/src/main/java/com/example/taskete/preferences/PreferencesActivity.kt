package com.example.taskete.preferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.taskete.R

class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        showPreferencesFragment()
    }

    private fun showPreferencesFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, PreferencesFragment())
            .commit()
    }

}