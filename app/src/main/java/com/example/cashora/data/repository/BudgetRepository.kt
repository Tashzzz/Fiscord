package com.example.cashora.data.repository

import com.example.cashora.data.dao.BudgetDao
import com.example.cashora.data.entity.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    suspend fun addBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    fun getBudgetsByMonth(userId: String, month: Int, year: Int): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(userId, month, year)
    }

    suspend fun getBudgetByCategory(userId: String, category: String, month: Int, year: Int): Budget? {
        return budgetDao.getBudgetByCategory(userId, category, month, year)
    }

    suspend fun getTotalBudgetAmount(userId: String, month: Int, year: Int): Double {
        return budgetDao.getTotalBudgetAmount(userId, month, year) ?: 0.0
    }

    suspend fun getTotalSpentAmount(userId: String, month: Int, year: Int): Double {
        return budgetDao.getTotalSpentAmount(userId, month, year) ?: 0.0
    }
} 