package com.example.gotask

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LauchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lauch)

        // Delay for 3 seconds before navigating to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@LauchActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // Closes the splash screen so it won't show up again on back press
        }, 3000) // 3000 milliseconds = 3 seconds
    }
}
