package com.example.cashora

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Logo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(R.layout.activity_logo)

        // Decide where to go after splash based on onboarding flag
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val hasCompletedOnboarding = prefs.getBoolean("onboarding_completed", false)
            val next = if (hasCompletedOnboarding) SignIn::class.java else Onboard1::class.java
            startActivity(Intent(this, next))
            finish()
        }, 2000)  // 2000 milliseconds = 2 seconds
    }
}