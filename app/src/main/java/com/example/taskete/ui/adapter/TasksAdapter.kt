package com.example.taskete.ui.adapter

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskete.R
import com.example.taskete.data.Priority
import com.example.taskete.data.Task
import com.example.taskete.db.TasksDAO
import com.example.taskete.extensions.dateFromString
import com.example.taskete.extensions.stringFromDate
import java.util.Date


class TasksAdapter(
        private val listener: RecyclerItemClickListener.OnItemClickListener
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = emptyList<Task>()
    private var selectedTasks: List<Task> = emptyList<Task>()
    private var defaultCardBg: Drawable? = null

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskCard: CardView = itemView.findViewById(R.id.taskCardView)
        val txtTitle: TextView = itemView.findViewById(R.id.etTitle)
        val chkIsDone: CheckBox = itemView.findViewById(R.id.chkIsDone)
        val imgPriority: ImageView = itemView.findViewById(R.id.priorityIcon)
        val txtDuedate: TextView = itemView.findViewById(R.id.etDueDate)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_card, parent, false)

        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.apply {
            resetViewHolder(this)
            txtTitle.text = tasks[position].title
            txtDuedate.apply {
                text = tasks[position].dueDate?.stringFromDate()
                setDateAlert(this)
            }

            setPriorityColor(this, tasks[position])
            strikeText(this, tasks[position])

            chkIsDone.apply {
                //Fix to checkbox not holding view state
                setOnCheckedChangeListener(null)
                setOnCheckedChangeListener { _, isChecked ->
                    tasks[position].isDone = isChecked

                    //Update selected task
                    TasksDAO(context).updateTask(tasks[position]).subscribe()
                    strikeText(holder, tasks[position])

                    //Wait until task is updated to perform action
                    Handler(context.mainLooper).postDelayed({
                        if (isChecked) {
                            listener.onItemCheck(this, position)
                        }
                    }, 500)
                }

                isChecked = tasks[position].isDone
            }

            //Highlight view
            taskCard.setOnClickListener {
                if (selectedTasks.contains(tasks[position]))
                    itemView.background = itemView.resources.getDrawable(R.drawable.bg_list_row, null)
                else {
                    itemView.background = defaultCardBg
                }
            }

        }
    }

    override fun getItemCount(): Int = tasks.size


    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun setDateAlert(dueDate: TextView) {
        val context = dueDate.context
        val txtDate = dueDate.text.toString()

        if (txtDate.isNotEmpty()) {
            val taskDate = txtDate.dateFromString()
            val actualDate = Date(System.currentTimeMillis())
            val diff = actualDate.compareTo(taskDate)

            if (diff > 0) {
                dueDate.setTextColor(ContextCompat.getColor(context, R.color.colorPriorityHigh))
                dueDate.setText(R.string.alertNoTimeLeft)
            } else {
                dueDate.setTextColor(ContextCompat.getColor(context, R.color.colorPriorityLow))
                dueDate.setText(R.string.alertTimeLeft)
            }
        }

    }

    private fun resetViewHolder(holder: TaskViewHolder) {
        if (defaultCardBg == null) {
            defaultCardBg = holder.taskCard.background
        } else {
            holder.taskCard.background = defaultCardBg
        }
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        selectedTasks = emptyList()
        notifyDataSetChanged()
    }

    fun getSelectedTasks(tasks: List<Task>) {
        selectedTasks = tasks
    }

    private fun strikeText(holder: TaskViewHolder, task: Task) {
        val text = holder.txtTitle
        val context = holder.txtTitle.context

        if (task.isDone) {
            text.paintFlags = text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            text.setTextColor(ContextCompat.getColor(context, R.color.colorTextDisabled))
        } else {
            text.paintFlags = text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            text.setTextColor(ContextCompat.getColor(context, R.color.colorTextEnabled))
        }
    }

    private fun setPriorityColor(holder: TaskViewHolder, task: Task) {
        val context = holder.itemView.context
        val icon = holder.imgPriority

        val highColor = ContextCompat.getColor(context, R.color.colorPriorityHigh);
        val mediumColor = ContextCompat.getColor(context, R.color.colorPriorityMedium);
        val lowColor = ContextCompat.getColor(context, R.color.colorPriorityLow);
        val noColor = ContextCompat.getColor(context, R.color.colorPriorityNotAssigned);

        when (task.priority) {
            Priority.LOW -> icon.setColorFilter(lowColor, PorterDuff.Mode.SRC_IN)
            Priority.MEDIUM -> icon.setColorFilter(mediumColor, PorterDuff.Mode.SRC_IN)
            Priority.HIGH -> icon.setColorFilter(highColor, PorterDuff.Mode.SRC_IN)
            else -> icon.setColorFilter(noColor, PorterDuff.Mode.SRC_IN)

        }
    }
}
