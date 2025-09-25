package com.example.cashora.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Double,
    val spent: Double = 0.0,
    val month: Int,
    val year: Int,
    val userId: String // Foreign key to User
) 