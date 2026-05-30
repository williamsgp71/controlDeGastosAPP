package com.example.data.dao

import androidx.room.*
import com.example.data.model.User
import com.example.data.model.Category
import com.example.data.model.ExpenseTransaction
import com.example.data.model.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Int): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId OR userId = -1 ORDER BY name ASC")
    fun getCategories(userId: Int): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Delete
    suspend fun deleteCategory(category: Category)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getTransactions(userId: Int): Flow<List<ExpenseTransaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND year = :year AND month = :month ORDER BY date DESC, id DESC")
    fun getTransactionsByPeriod(userId: Int, year: Int, month: Int): Flow<List<ExpenseTransaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND year = :year ORDER BY date DESC, id DESC")
    fun getTransactionsByYear(userId: Int, year: Int): Flow<List<ExpenseTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: ExpenseTransaction): Long

    @Delete
    suspend fun deleteTransaction(tx: ExpenseTransaction)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month LIMIT 1")
    fun getBudgetFlow(userId: Int, year: Int, month: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND year = :year AND month = :month LIMIT 1")
    suspend fun getBudget(userId: Int, year: Int, month: Int): Budget?

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: Int): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget): Long
}
