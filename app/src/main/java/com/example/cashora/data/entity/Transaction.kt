package com.example.cashora.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val type: String, // "INCOME" or "EXPENSE"
    val date: Date,
    val userId: String // Foreign key to User
) 