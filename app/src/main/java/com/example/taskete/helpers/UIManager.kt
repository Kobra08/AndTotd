package com.example.taskete.helpers

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.example.taskete.R

object UIManager {
    fun showMessage(context: Context, message: String) {
        KeyboardUtil.hideKeyboard(context as Activity)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
            show()
        }
    }

    fun showMessageLong(context: Context, message: String) {
        KeyboardUtil.hideKeyboard(context as Activity)
        Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
            show()
        }
    }

    fun showDisabledFeature(context: Context, component: String) {
        showMessage(
            context,
            "$component ${context.getText(R.string.featureIsDisabled)}"
        )
    }

    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    fun hide(view: View) {
        view.visibility = View.GONE
    }
}