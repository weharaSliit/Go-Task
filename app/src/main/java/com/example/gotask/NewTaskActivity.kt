package com.example.gotask

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class NewTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_task)

        val taskText = findViewById<EditText>(R.id.newTaskText)
        val saveButton = findViewById<Button>(R.id.newTaskButton)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)

        timePicker.setIs24HourView(true)

        val sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE)

        val taskPosition = intent.getIntExtra("task_position", -1)
        val existingTask = intent.getStringExtra("task_text")

        if (taskPosition != -1 && existingTask != null) {
            taskText.setText(existingTask)
            saveButton.setOnClickListener {
                val newTask = taskText.text.toString()
                val hour = timePicker.hour
                val minute = timePicker.minute
                val timeInMillis = (hour * 60L + minute) * 60 * 1000 // Calculate milliseconds

                if (newTask.isNotEmpty()) {
                    updateTask(taskPosition, existingTask, newTask, timeInMillis, sharedPref)
                    finish()
                }
            }
        } else {
            saveButton.setOnClickListener {
                val task = taskText.text.toString()
                val hour = timePicker.hour
                val minute = timePicker.minute
                val timeInMillis = (hour * 60L + minute) * 60 * 1000 // Calculate milliseconds

                if (task.isNotEmpty()) {
                    addNewTask(task, timeInMillis, sharedPref)
                    finish()
                }
            }
        }
    }

    private fun addNewTask(task: String, timeInMillis: Long, sharedPref: SharedPreferences) {
        val taskList = sharedPref.getStringSet("taskList", setOf())?.toMutableSet() ?: mutableSetOf()
        taskList.add("$task||$timeInMillis")
        with(sharedPref.edit()) {
            putStringSet("taskList", taskList)
            apply()
        }
    }

    private fun updateTask(position: Int, oldTask: String, newTask: String, timeInMillis: Long, sharedPref: SharedPreferences) {
        val taskList = sharedPref.getStringSet("taskList", setOf())?.toMutableSet() ?: mutableSetOf()
        taskList.remove(oldTask)
        taskList.add("$newTask||$timeInMillis")
        with(sharedPref.edit()) {
            putStringSet("taskList", taskList)
            apply()
        }
    }
}
