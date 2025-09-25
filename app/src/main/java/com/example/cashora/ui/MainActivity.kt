package com.example.cashora.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashora.R
import com.example.cashora.data.AppDatabase
import com.example.cashora.data.PreferencesManager
import com.example.cashora.data.entity.Transaction
import com.example.cashora.data.repository.TransactionRepository
import com.example.cashora.databinding.ActivityMainBinding
import com.example.cashora.notification.NotificationManager
import com.example.cashora.ui.adapters.RecentTransactionsAdapter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var adapter: RecentTransactionsAdapter

    private val calendar = Calendar.getInstance()

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Room database and repository
        val database = AppDatabase.getDatabase(applicationContext)
        transactionRepository = TransactionRepository(database.transactionDao())
        preferencesManager = PreferencesManager(this)
        notificationManager = NotificationManager(this)

        // Request notification permission
        requestNotificationPermission()

        notificationManager.scheduleDailyReminder()

        setupBottomNavigation()
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()

        binding.btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateDashboard()
        }

        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateDashboard()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can show notifications
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Notification permission denied. You won't receive budget alerts.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
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

    private fun setupMonthDisplay() {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvCurrentMonth.text = dateFormat.format(calendar.time)
    }

    private fun setupSummaryCards() {
        lifecycleScope.launch {
            val userId = getCurrentUserId()
            val currency = preferencesManager.getCurrency()

            val startDate = getStartOfMonth(calendar.time)
            val endDate = getEndOfMonth(calendar.time)

            val totalIncome = transactionRepository.getTotalAmountByDateRange(
                userId, "INCOME", startDate.time, endDate.time
            )
            val totalExpenses = transactionRepository.getTotalAmountByDateRange(
                userId, "EXPENSE", startDate.time, endDate.time
            )
            val balance = totalIncome - totalExpenses

            binding.tvIncomeAmount.text = String.format("%s %.2f", currency, totalIncome)
            binding.tvExpenseAmount.text = String.format("%s %.2f", currency, totalExpenses)
            binding.tvBalanceAmount.text = String.format("%s %.2f", currency, balance)

            // Budget progress
            val budget = preferencesManager.getBudget()
            if (budget.month == calendar.get(Calendar.MONTH) &&
                budget.year == calendar.get(Calendar.YEAR) &&
                budget.amount > 0) {

                val percentage = (totalExpenses / budget.amount) * 100
                binding.progressBudget.progress = percentage.toInt().coerceAtMost(100)
                binding.tvBudgetStatus.text = String.format(
                    "%.1f%% of %s %.2f", percentage, currency, budget.amount
                )

                if (percentage >= 100) {
                    binding.tvBudgetStatus.setTextColor(Color.RED)
                } else if (percentage >= 80) {
                    binding.tvBudgetStatus.setTextColor(Color.parseColor("#FFA500")) // Orange
                } else {
                    binding.tvBudgetStatus.setTextColor(Color.GREEN)
                }
            } else {
                binding.progressBudget.progress = 0
                binding.tvBudgetStatus.text = getString(R.string.no_budget_set)
                binding.tvBudgetStatus.setTextColor(Color.GRAY)
            }
        }
    }

    private fun setupCategoryChart() {
        lifecycleScope.launch {
            try {
                val userId = getCurrentUserId()
                val startDate = getStartOfMonth(calendar.time)
                val endDate = getEndOfMonth(calendar.time)

                Log.d("MainActivity", "Fetching transactions for date range: $startDate to $endDate")
                Log.d("MainActivity", "User ID: $userId")
                
                val transactions = transactionRepository.getTransactionsByType(userId, "EXPENSE").first()
                Log.d("MainActivity", "Total transactions found: ${transactions.size}")
                
                // Add sample data for testing if no transactions exist
                val expensesByCategory = if (transactions.isEmpty()) {
                    Log.d("MainActivity", "No transactions found, using sample data")
                    mapOf(
                        "Food" to 500.0,
                        "Transport" to 300.0,
                        "Shopping" to 200.0,
                        "Bills" to 400.0
                    )
                } else {
                    transactions
                        .filter { it.date in startDate..endDate }
                        .groupBy { it.category }
                        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                }
                
                Log.d("MainActivity", "Filtered transactions by date: ${expensesByCategory.size} categories")
                expensesByCategory.forEach { (category, amount) ->
                    Log.d("MainActivity", "Category: $category, Amount: $amount")
                }

                if (expensesByCategory.isEmpty()) {
                    Log.d("MainActivity", "No expenses found for the current month")
                    binding.pieChart.visibility = View.GONE
                    return@launch
                }

                binding.pieChart.visibility = View.VISIBLE
                
                // Configure the chart
                binding.pieChart.apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(Color.TRANSPARENT)
                    holeRadius = 58f
                    transparentCircleRadius = 61f
                    setDrawCenterText(true)
                    setCenterText("Expenses")
                    setCenterTextSize(16f)
                    setCenterTextColor(Color.BLACK)
                    
                    // Configure legend
                    legend.apply {
                        isEnabled = true
                        setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM)
                        setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER)
                        setOrientation(Legend.LegendOrientation.HORIZONTAL)
                        setDrawInside(false)
                        textSize = 12f
                        textColor = Color.BLACK
                    }
                    
                    setEntryLabelColor(Color.BLACK)
                    setEntryLabelTextSize(12f)
                    animateY(1000)
                }

                val entries = ArrayList<PieEntry>()
                val colors = ArrayList<Int>()

                expensesByCategory.forEach { (category, amount) ->
                    entries.add(PieEntry(amount.toFloat(), category))
                    colors.add(ColorTemplate.MATERIAL_COLORS[entries.size % ColorTemplate.MATERIAL_COLORS.size])
                }

                Log.d("MainActivity", "Created ${entries.size} pie chart entries")

                val dataSet = PieDataSet(entries, "Categories")
                dataSet.colors = colors
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueLinePart1Length = 0.4f
                dataSet.valueLinePart2Length = 0.4f
                dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

                val pieData = PieData(dataSet)
                pieData.setValueFormatter(PercentFormatter(binding.pieChart))
                binding.pieChart.data = pieData
                binding.pieChart.invalidate()

                Log.d("MainActivity", "Pie chart data set and invalidated")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up pie chart", e)
            }
        }
    }

    private fun setupRecentTransactions() {
        lifecycleScope.launch {
            val userId = getCurrentUserId()
            val startDate = getStartOfMonth(calendar.time)
            val endDate = getEndOfMonth(calendar.time)

            val transactions = transactionRepository.getAllTransactions(userId).first()
                .filter { it.date in startDate..endDate }
                .sortedByDescending { it.date }
                .take(5)

            adapter = RecentTransactionsAdapter(transactions, preferencesManager.getCurrency())
            binding.recyclerRecentTransactions.adapter = adapter

            binding.tvViewAllTransactions.setOnClickListener {
                startActivity(Intent(this@MainActivity, TransactionsActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }

    private fun updateDashboard() {
        setupMonthDisplay()
        setupSummaryCards()
        setupCategoryChart()
        setupRecentTransactions()
    }

    private fun getCurrentUserId(): String {
        // TODO: Implement this method to get the current user's ID
        // This could be stored in SharedPreferences or retrieved from your authentication system
        return "current_user_id"
    }

    private fun getStartOfMonth(date: java.util.Date): java.util.Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    private fun getEndOfMonth(date: java.util.Date): java.util.Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
        notificationManager.checkBudgetAndNotify()
    }
}