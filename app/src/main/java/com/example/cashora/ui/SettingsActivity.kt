package com.example.cashora.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cashora.R
import com.example.cashora.data.PreferencesManager
import com.example.cashora.data.TransactionRepository
import com.example.cashora.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)
        transactionRepository = TransactionRepository(this)

        setupBottomNavigation()
        setupSettings()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_settings
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun setupSettings() {
        // Currency spinner
        val currencies = listOf("USD", "LKR", "EUR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCurrency.adapter = adapter
        val current = preferencesManager.getCurrency()
        val currentIndex = currencies.indexOf(current).takeIf { it >= 0 } ?: 0
        binding.spinnerCurrency.setSelection(currentIndex)
        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                preferencesManager.setCurrency(currencies[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) { }
        }

        // Notification toggles
        binding.switchBudgetAlerts.isChecked = preferencesManager.isNotificationEnabled()
        binding.switchBudgetAlerts.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationEnabled(isChecked)
        }

        binding.switchDailyReminders.isChecked = preferencesManager.isReminderEnabled()
        binding.switchDailyReminders.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setReminderEnabled(isChecked)
        }

        // Backup / Restore
        binding.btnBackupData.setOnClickListener {
            val ok = transactionRepository.backupToInternalStorage(this)
            Toast.makeText(this, if (ok) getString(R.string.backup_success) else getString(R.string.backup_failed), Toast.LENGTH_SHORT).show()
        }

        binding.btnRestoreData.setOnClickListener {
            val ok = transactionRepository.restoreFromInternalStorage(this)
            Toast.makeText(this, if (ok) getString(R.string.restore_success) else getString(R.string.restore_failed), Toast.LENGTH_SHORT).show()
        }
    }
}