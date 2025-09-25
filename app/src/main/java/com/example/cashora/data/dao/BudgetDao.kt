package com.example.cashora.data.dao

import androidx.room.*
import com.example.cashora.data.entity.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    fun getBudgetsByMonth(userId: String, month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category AND month = :month AND year = :year")
    suspend fun getBudgetByCategory(userId: String, category: String, month: Int, year: Int): Budget?

    @Query("SELECT SUM(amount) FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getTotalBudgetAmount(userId: String, month: Int, year: Int): Double?

    @Query("SELECT SUM(spent) FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getTotalSpentAmount(userId: String, month: Int, year: Int): Double?
} 