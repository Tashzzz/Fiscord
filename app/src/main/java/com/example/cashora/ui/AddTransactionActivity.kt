package com.example.cashora.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashora.R
import com.example.cashora.data.AppDatabase
import com.example.cashora.data.entity.Transaction
import com.example.cashora.data.repository.TransactionRepository
import com.example.cashora.databinding.ActivityAddTransactionBinding
import com.example.cashora.notification.NotificationManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var notificationManager: NotificationManager
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        private val DEFAULT_CATEGORIES = arrayOf(
            "Food", "Transport", "Shopping", "Bills", "Entertainment",
            "Health", "Education", "Other"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        transactionRepository = TransactionRepository(database.transactionDao())
        notificationManager = NotificationManager(this)

        setupCategorySpinner()
        setupDatePicker()
        setupButtons()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, DEFAULT_CATEGORIES
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormatter.format(calendar.time))

        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.etDate.setText(dateFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.etTitle.text.toString().trim().isEmpty()) {
            binding.etTitle.error = getString(R.string.field_required)
            isValid = false
        }

        if (binding.etAmount.text.toString().trim().isEmpty()) {
            binding.etAmount.error = getString(R.string.field_required)
            isValid = false
        }

        return isValid
    }

    private fun saveTransaction() {
        try {
            val description = binding.etTitle.text.toString().trim()
            val amount = binding.etAmount.text.toString().toDouble()
            val category = binding.spinnerCategory.selectedItem.toString()
            val date = calendar.time
            val type = if (binding.radioExpense.isChecked) "EXPENSE" else "INCOME"
            val userId = getCurrentUserId() // You need to implement this method to get the current user's ID

            val transaction = Transaction(
                amount = amount,
                description = description,
                category = category,
                type = type,
                date = date,
                userId = userId
            )

            lifecycleScope.launch {
                transactionRepository.addTransaction(transaction)
                notificationManager.checkBudgetAndNotify()

                runOnUiThread {
                    Toast.makeText(
                        this@AddTransactionActivity,
                        getString(R.string.transaction_added),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                getString(R.string.error_adding_transaction),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getCurrentUserId(): String {
        // TODO: Implement this method to get the current user's ID
        // This could be stored in SharedPreferences or retrieved from your authentication system
        return "current_user_id"
    }
}