package com.example.taskete.ui.activities

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.example.taskete.R


class TaskSelectionMode(
        private val listener: TaskSelection
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.delete_item_menu, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_delete -> {
                listener.showDeleteConfirmationDialog()
                true
            }
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        listener.resetSelection()
    }
}

interface TaskSelection {
    fun showDeleteConfirmationDialog()
    fun resetSelection()
}
