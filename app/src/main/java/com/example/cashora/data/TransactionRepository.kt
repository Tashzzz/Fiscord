package com.example.cashora.data

import android.content.Context
import android.content.SharedPreferences
import com.example.cashora.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class TransactionRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        TRANSACTIONS_PREFS, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val TRANSACTIONS_PREFS = "transactions_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
    }

    fun saveTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()

        // Check if transaction exists (for updates)
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }
        if (existingIndex >= 0) {
            transactions[existingIndex] = transaction
        } else {
            transactions.add(transaction)
        }

        saveAllTransactions(transactions)
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeIf { it.id == transactionId }
        saveAllTransactions(transactions)
    }

    fun getAllTransactions(): List<Transaction> {
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null)
        return if (transactionsJson != null) {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val transactionsList: List<Map<String, Any>> = gson.fromJson(transactionsJson, type)
            transactionsList.map { Transaction.fromMap(it) }
        } else {
            emptyList()
        }
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val transactionsMapList = transactions.map { it.toMap() }
        val transactionsJson = gson.toJson(transactionsMapList)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
    }

    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return getAllTransactions().filter { transaction ->
            val calendar = Calendar.getInstance()
            calendar.time = transaction.date
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }

    fun getExpensesForMonth(month: Int, year: Int): List<Transaction> {
        return getTransactionsForMonth(month, year).filter { it.isExpense }
    }

    fun getIncomeForMonth(month: Int, year: Int): List<Transaction> {
        return getTransactionsForMonth(month, year).filter { !it.isExpense }
    }

    fun getTotalExpensesForMonth(month: Int, year: Int): Double {
        return getExpensesForMonth(month, year).sumOf { it.amount }
    }

    fun getTotalIncomeForMonth(month: Int, year: Int): Double {
        return getIncomeForMonth(month, year).sumOf { it.amount }
    }

    fun getExpensesByCategory(month: Int, year: Int): Map<String, Double> {
        val expenses = getExpensesForMonth(month, year)
        return expenses.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }

    fun backupToInternalStorage(context: Context): Boolean {
        try {
            val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
            context.openFileOutput("transactions_backup.json", Context.MODE_PRIVATE).use {
                it.write(transactionsJson?.toByteArray() ?: "[]".toByteArray())
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun restoreFromInternalStorage(context: Context): Boolean {
        try {
            context.openFileInput("transactions_backup.json").use { inputStream ->
                val transactionsJson = inputStream.bufferedReader().use { it.readText() }
                sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
