package com.example.cashora.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cashora.data.dao.BudgetDao
import com.example.cashora.data.dao.TransactionDao
import com.example.cashora.data.dao.UserDao
import com.example.cashora.data.entity.Budget
import com.example.cashora.data.entity.Transaction
import com.example.cashora.data.entity.User
import com.example.cashora.util.DateConverter

@Database(
    entities = [User::class, Transaction::class, Budget::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashora_database"
                )
                    // When schema changes, wipe and rebuild the DB to avoid migration crashes.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 