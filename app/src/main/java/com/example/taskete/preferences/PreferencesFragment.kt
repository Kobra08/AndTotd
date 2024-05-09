package com.example.taskete.preferences

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.taskete.ui.activities.AboutMeActivity
import com.example.taskete.R


class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        launchAboutMeActivity()
    }

    private fun launchAboutMeActivity() {
        preferenceManager.findPreference<Preference>("showAboutMeSection")
            ?.setOnPreferenceClickListener {
                Intent(activity, AboutMeActivity::class.java).apply {
                    startActivity(this)
                }
                true
            }
    }
}