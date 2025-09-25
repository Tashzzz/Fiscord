package com.example.cashora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cashora.ui.theme.AppTheme

class GetStarted : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                GetStartedScreen(
                    onSignIn = { startActivity(Intent(this, SignIn::class.java)) },
                    onSignUp = { startActivity(Intent(this, SignUp::class.java)) }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GetStartedScreen(
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Fiscord") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Track expenses, set budgets and get reminders.",
                textAlign = TextAlign.Center
            )
            Button(onClick = onSignIn) { Text("Sign in") }
            Button(onClick = onSignUp) { Text("Create account") }
        }
    }
}