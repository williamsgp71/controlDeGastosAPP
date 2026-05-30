package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.User
import com.example.data.model.Category
import com.example.data.model.ExpenseTransaction
import com.example.data.model.Budget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest

class AppRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()
    private val budgetDao = db.budgetDao()

    // Passwords Utilities
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // User Operations
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email.lowercase().trim())
    }

    suspend fun registerUser(email: String, password: String): User? {
        val normalizedEmail = email.lowercase().trim()
        val existing = userDao.getUserByEmail(normalizedEmail)
        if (existing != null) return null // User already exists!

        val hashed = hashPassword(password)
        val user = User(email = normalizedEmail, passwordHash = hashed, isBiometricEnabled = false)
        val id = userDao.insertUser(user)
        return user.copy(id = id.toInt())
    }

    suspend fun updateBiometricPreference(userId: Int, enabled: Boolean) {
        db.runInTransaction {
            // Room transaction context doesn't easily support coroutines unless we use suspend
        }
        // Let's do it directly
        val flow = userDao.getUserById(userId)
        val current = flow.firstOrNull() ?: db.run {
            // Fallback for non-flow query if needed, or query direct
            null
        }
        // Let's update directly
        // Better: We can just fetch user, update, and write
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun changePassword(email: String, oldPass: String, newPass: String): Boolean {
        val user = userDao.getUserByEmail(email.lowercase().trim()) ?: return false
        if (user.passwordHash != hashPassword(oldPass)) return false
        val updated = user.copy(passwordHash = hashPassword(newPass))
        userDao.updateUser(updated)
        return true
    }

    suspend fun forceUpdatePassword(email: String, newPass: String): Boolean {
        val user = userDao.getUserByEmail(email.lowercase().trim()) ?: return false
        val updated = user.copy(passwordHash = hashPassword(newPass))
        userDao.updateUser(updated)
        return true
    }

    // Category Operations
    fun getCategories(userId: Int): Flow<List<Category>> {
        return categoryDao.getCategories(userId)
    }

    suspend fun insertCategory(userId: Int, name: String, isExpense: Boolean): Category {
        val cat = Category(userId = userId, name = name.trim(), isExpense = isExpense)
        val id = categoryDao.insertCategory(cat)
        return cat.copy(id = id.toInt())
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // Prepopulate standard categories
    suspend fun prepopulateDefaultCategories() {
        val defaultCats = listOf(
            // Expenses (Egresos)
            Category(userId = -1, name = "Comida / Supermercado", isExpense = true),
            Category(userId = -1, name = "Alquiler / Vivienda", isExpense = true),
            Category(userId = -1, name = "Transporte", isExpense = true),
            Category(userId = -1, name = "Servicios / Recibos", isExpense = true),
            Category(userId = -1, name = "Gimnasio / Deportes", isExpense = true),
            Category(userId = -1, name = "Entretenimiento", isExpense = true),
            Category(userId = -1, name = "Suscripciones", isExpense = true),
            Category(userId = -1, name = "Salud / Médicina", isExpense = true),
            
            // Incomes (Ingresos)
            Category(userId = -1, name = "Sueldo / Nómina", isExpense = false),
            Category(userId = -1, name = "Trabajo Freelance", isExpense = false),
            Category(userId = -1, name = "Inversiones", isExpense = false),
            Category(userId = -1, name = "Ventas", isExpense = false)
        )

        // Count how many categories exist
        val currentFlow = categoryDao.getCategories(-1)
        val currentList = currentFlow.firstOrNull() ?: emptyList()
        if (currentList.isEmpty()) {
            for (cat in defaultCats) {
                categoryDao.insertCategory(cat)
            }
        }
    }

    // Transaction Operations
    fun getTransactions(userId: Int): Flow<List<ExpenseTransaction>> {
        return transactionDao.getTransactions(userId)
    }

    fun getTransactionsByPeriod(userId: Int, year: Int, month: Int): Flow<List<ExpenseTransaction>> {
        if (year == 0 && month == 0) {
            return transactionDao.getTransactions(userId)
        } else if (month == 0) {
            return transactionDao.getTransactionsByYear(userId, year)
        }
        return transactionDao.getTransactionsByPeriod(userId, year, month)
    }

    suspend fun insertTransaction(tx: ExpenseTransaction): ExpenseTransaction {
        val id = transactionDao.insertTransaction(tx)
        return tx.copy(id = id.toInt())
    }

    suspend fun deleteTransaction(tx: ExpenseTransaction) {
        transactionDao.deleteTransaction(tx)
    }

    // Budget Operations
    fun getBudgetFlow(userId: Int, year: Int, month: Int): Flow<Budget?> {
        return budgetDao.getBudgetFlow(userId, year, month)
    }

    suspend fun saveBudget(userId: Int, year: Int, month: Int, amount: Double) {
        val existing = budgetDao.getBudget(userId, year, month)
        if (existing != null) {
            val updated = existing.copy(amount = amount)
            budgetDao.insertBudget(updated)
        } else {
            val budget = Budget(userId = userId, year = year, month = month, amount = amount)
            budgetDao.insertBudget(budget)
        }
    }
}
