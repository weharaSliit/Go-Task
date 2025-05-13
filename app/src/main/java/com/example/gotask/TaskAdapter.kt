package com.example.gotask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private val taskList: MutableList<String>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val taskTimes: MutableMap<String, String> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.task_layout, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        val taskDetails = task.split("||")
        val taskName = taskDetails[0]
        val taskTime = taskTimes[taskName] ?: "00:00:00"

        holder.todoCheckBox.text = taskName
        holder.timerTextView.text = taskTime
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun updateTaskTime(taskName: String, time: String) {
        taskTimes[taskName] = time
        notifyDataSetChanged()
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoCheckBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val timerTextView: TextView = itemView.findViewById(R.id.timerTextView)
    }
}
