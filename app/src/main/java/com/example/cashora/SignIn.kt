package com.example.cashora

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashora.data.AppDatabase
import com.example.cashora.data.repository.UserRepository
import com.example.cashora.ui.MainActivity
import kotlinx.coroutines.launch

class SignIn : AppCompatActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        // Initialize Room database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get references to input fields
        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)

        // Get reference to the Sign In button
        val loginButton = findViewById<Button>(R.id.login_button)

        // "Don't have an account? Register" link
        val signUpLink = findViewById<TextView>(R.id.sign_up_link)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = userRepository.loginUser(email, password)
                if (user != null) {
                    Toast.makeText(this@SignIn, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignIn, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@SignIn, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // If user doesn't have an account, navigate to SignUp
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }
}
