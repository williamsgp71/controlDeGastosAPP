package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String, // Stored securely
    val isBiometricEnabled: Boolean = false
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // -1 for default global categories
    val name: String,
    val isExpense: Boolean
)

@Entity(tableName = "transactions")
data class ExpenseTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val currency: String, // "S/" or "$"
    val description: String,
    val categoryId: Int,
    val date: String, // dd/MM/yyyy
    val year: Int,
    val month: Int, // 1 to 12
    val isExpense: Boolean
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val year: Int,
    val month: Int, // 1 to 12
    val amount: Double
)
