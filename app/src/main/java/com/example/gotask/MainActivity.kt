package com.example.gotask

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var tasksAdapter: TaskAdapter
    private lateinit var sharedPref: SharedPreferences
    private var taskList: MutableList<String> = mutableListOf()
    private val taskTimers: MutableMap<String, CountDownTimer> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences("myPref", Context.MODE_PRIVATE)

        tasksRecyclerView = findViewById(R.id.tasksRecyclerView)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksAdapter = TaskAdapter(taskList)
        tasksRecyclerView.adapter = tasksAdapter

        loadTasks()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this, NewTaskActivity::class.java))
        }

        setupSwipeToDeleteAndUpdate()
    }

    private fun setupSwipeToDeleteAndUpdate() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {
                    ItemTouchHelper.LEFT -> showDeleteConfirmationDialog(position)
                    ItemTouchHelper.RIGHT -> {
                        val task = taskList[position]
                        val intent = Intent(this@MainActivity, NewTaskActivity::class.java).apply {
                            putExtra("task_position", position)
                            putExtra("task_text", task)
                        }
                        startActivity(intent)
                    }
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTask(position) }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                tasksAdapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun deleteTask(position: Int) {
        val task = taskList[position]
        // Cancel any existing countdown timer for the task
        taskTimers[task]?.cancel()
        taskTimers.remove(task)

        val taskListSet = sharedPref.getStringSet("taskList", setOf())?.toMutableSet() ?: mutableSetOf()
        taskListSet.remove(task)
        with(sharedPref.edit()) {
            putStringSet("taskList", taskListSet)
            apply()
        }
        taskList.removeAt(position)
        tasksAdapter.notifyItemRemoved(position)
    }

    private fun loadTasks() {
        val savedTasks = sharedPref.getStringSet("taskList", setOf())?.toMutableList()
        if (savedTasks != null) {
            taskList.clear()
            taskList.addAll(savedTasks)
            tasksAdapter.notifyDataSetChanged()
            for (task in taskList) {
                val taskDetails = task.split("||")
                if (taskDetails.size == 2) {
                    val taskName = taskDetails[0]
                    val timeInMillis = taskDetails[1].toLong()
                    startTaskTimer(taskName, timeInMillis)
                }
            }
        }
    }

    private fun startTaskTimer(taskName: String, timeInMillis: Long) {
        val timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val formattedTime = formatTime(millisUntilFinished)
                tasksAdapter.updateTaskTime(taskName, formattedTime)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onFinish() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    val vibrator = vibratorManager.defaultVibrator
                    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                }
            }
        }.start()
        taskTimers[taskName] = timer
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }
}
