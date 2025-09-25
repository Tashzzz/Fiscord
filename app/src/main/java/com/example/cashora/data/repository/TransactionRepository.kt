package com.example.cashora.data.repository

import com.example.cashora.data.dao.TransactionDao
import com.example.cashora.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getAllTransactions(userId: String): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(userId)
    }

    fun getTransactionsByType(userId: String, type: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    fun getTransactionsByCategory(userId: String, category: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(userId, category)
    }

    suspend fun getTotalAmountByDateRange(userId: String, type: String, startDate: Long, endDate: Long): Double {
        return transactionDao.getTotalAmountByDateRange(userId, type, startDate, endDate) ?: 0.0
    }
} 