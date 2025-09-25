package com.example.cashora.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.cashora.R
import com.example.cashora.data.AppDatabase
import com.example.cashora.data.PreferencesManager
import com.example.cashora.data.entity.Transaction
import com.example.cashora.data.repository.TransactionRepository
import com.example.cashora.databinding.ActivityTransactionsBinding
import com.example.cashora.ui.adapters.TransactionsAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionsActivity : AppCompatActivity(), TransactionsAdapter.OnTransactionClickListener {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: TransactionsAdapter

    private val calendar = Calendar.getInstance()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        transactionRepository = TransactionRepository(database.transactionDao())
        preferencesManager = PreferencesManager(this)

        setupBottomNavigation()
        setupMonthSelector()
        setupFilterSpinner()
        setupTransactionsList()

        binding.fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_transactions
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_transactions -> true
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupMonthSelector() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = dateFormat.format(calendar.time)

        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateTransactionsList()
            binding.tvCurrentMonth.text = dateFormat.format(calendar.time)
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateTransactionsList()
            binding.tvCurrentMonth.text = dateFormat.format(calendar.time)
        }
    }

    private fun setupFilterSpinner() {
        val filters = arrayOf("All", "Expenses", "Income")
        val spinnerAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, filters
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = filters[position].lowercase()
                updateTransactionsList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTransactionsList() {
        adapter = TransactionsAdapter(emptyList(), preferencesManager.getCurrency(), this)
        binding.recyclerTransactions.adapter = adapter
        observeTransactions()
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            val userId = getCurrentUserId()
            when (currentFilter) {
                "expenses" -> transactionRepository.getTransactionsByType(userId, "EXPENSE").collect { transactions ->
                    adapter.updateTransactions(transactions)
                }
                "income" -> transactionRepository.getTransactionsByType(userId, "INCOME").collect { transactions ->
                    adapter.updateTransactions(transactions)
                }
                else -> transactionRepository.getAllTransactions(userId).collect { transactions ->
                    adapter.updateTransactions(transactions)
                }
            }
        }
    }

    private fun updateTransactionsList() {
        observeTransactions()
    }

    override fun onTransactionClick(transaction: Transaction) {
        val intent = Intent(this, EditTransactionActivity::class.java).apply {
            putExtra("transaction_id", transaction.id)
            putExtra("transaction_description", transaction.description)
            putExtra("transaction_amount", transaction.amount)
            putExtra("transaction_category", transaction.category)
            putExtra("transaction_date", transaction.date.time)
            putExtra("transaction_type", transaction.type)
        }
        startActivity(intent)
    }

    override fun onTransactionLongClick(transaction: Transaction) {
        val intent = Intent(this, DeleteTransactionActivity::class.java).apply {
            putExtra("transaction_id", transaction.id)
            putExtra("transaction_description", transaction.description)
        }
        startActivity(intent)
    }

    private fun getCurrentUserId(): String {
        // TODO: Implement this method to get the current user's ID
        // This could be stored in SharedPreferences or retrieved from your authentication system
        return "current_user_id"
    }

    override fun onResume() {
        super.onResume()
        updateTransactionsList()
    }
}