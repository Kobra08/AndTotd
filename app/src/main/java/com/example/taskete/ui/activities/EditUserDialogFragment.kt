package com.example.taskete.ui.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.taskete.R


class EditUserDialogFragment : DialogFragment() {

    private lateinit var etUsername: EditText
    private lateinit var listener: EditDialogListener

    companion object {
        const val TAG = "EditUserFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertBuilder = AlertDialog.Builder(context)
        activity?.layoutInflater?.inflate(R.layout.edit_username_layout, null)?.let {
            etUsername = it.findViewById(R.id.etNewUsername)

            alertBuilder.apply {
                setView(it)
                setTitle("Change username")
                setPositiveButton(R.string.change_name_OK) { _, _ ->
                    listener.onPositiveClick(etUsername)
                }
                setNegativeButton(
                    R.string.change_name_CANCEL
                ) { _, _ -> }
                    .setCancelable(false)
            }
        }

        return alertBuilder.create()
    }

    override fun onAttach(context: Context) {
        try {
            listener = context as EditDialogListener
        } catch (e: Exception) {
            Log.d(TAG, "Must implement EditDialogListener because ${e.message}")
        }
        super.onAttach(context)
    }
}

interface EditDialogListener {
    fun onPositiveClick(editText: EditText)
}