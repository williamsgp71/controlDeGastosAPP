package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Budget
import com.example.data.model.Category
import com.example.data.model.ExpenseTransaction
import com.example.data.model.User
import com.example.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Initial Login and App Status
    var loggedInUser = MutableStateFlow<User?>(null)
        private set

    // Filtering State
    val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR)) // e.g. 2026
    val selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-12, e.g. 5
    val selectedAnalysisCurrency = MutableStateFlow("S/") // "S/" or "$"

    // Form states
    var activeTab = MutableStateFlow("Historial de Transacciones") // "Historial de Transacciones", "Categorías (11)", "Seguridad Biométrica"

    // Feedback States
    val loginError = MutableStateFlow<String?>(null)
    val loginSuccess = MutableStateFlow(false)
    val registerError = MutableStateFlow<String?>(null)
    val registerSuccess = MutableStateFlow<String?>(null)
    val changePasswordStatus = MutableStateFlow<String?>(null)

    // Exchange Rate constant (1 USD = 3.75 PEN)
    private val exchangeRate = 3.75

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)
        
        // Initialize default global categories
        viewModelScope.launch {
            repository.prepopulateDefaultCategories()
        }
    }

    // Biometric Preferences Saving
    fun setBiometricEnabled(userId: Int, enabled: Boolean) {
        viewModelScope.launch {
            val user = loggedInUser.value
            if (user != null && user.id == userId) {
                val updated = user.copy(isBiometricEnabled = enabled)
                repository.updateUser(updated)
                loggedInUser.value = updated
            }
        }
    }

    // Login Flow
    fun login(email: String, oldPasswordPlain: String) {
        viewModelScope.launch {
            loginError.value = null
            if (email.isBlank() || oldPasswordPlain.isBlank()) {
                loginError.value = "Correo y contraseña no pueden estar vacíos"
                return@launch
            }

            val user = repository.getUserByEmail(email)
            if (user == null) {
                loginError.value = "El usuario no existe."
                return@launch
            }

            val hashed = repository.hashPassword(oldPasswordPlain)
            if (user.passwordHash == hashed) {
                loggedInUser.value = user
                loginSuccess.value = true
                loginError.value = null
            } else {
                loginError.value = "Contraseña incorrecta."
            }
        }
    }

    // Login directly used for biometric login
    fun loginBiometricDirect(email: String) {
        viewModelScope.launch {
            loginError.value = null
            val user = repository.getUserByEmail(email)
            if (user != null && user.isBiometricEnabled) {
                loggedInUser.value = user
                loginSuccess.value = true
            } else {
                loginError.value = "Autenticación biométrica no configurada para este correo."
            }
        }
    }

    // Register Flow
    fun register(email: String, pass1: String, pass2: String) {
        viewModelScope.launch {
            registerError.value = null
            registerSuccess.value = null
            
            if (email.isBlank() || pass1.isBlank() || pass2.isBlank()) {
                registerError.value = "Todos los campos son obligatorios"
                return@launch
            }
            if (pass1 != pass2) {
                registerError.value = "Las contraseñas no coinciden"
                return@launch
            }
            if (pass1.length < 6) {
                registerError.value = "La contraseña debe tener al menos 6 caracteres"
                return@launch
            }

            val registered = repository.registerUser(email, pass1)
            if (registered != null) {
                registerSuccess.value = "¡Usuario registrado con éxito! Ya puedes iniciar sesión."
            } else {
                registerError.value = "El correo ya está registrado por otro usuario"
            }
        }
    }

    // Password reset / change
    fun changePassword(email: String, oldPass: String, newPass: String, confPass: String) {
        viewModelScope.launch {
            changePasswordStatus.value = null
            if (email.isBlank() || oldPass.isBlank() || newPass.isBlank() || confPass.isBlank()) {
                changePasswordStatus.value = "Todos los campos son obligatorios"
                return@launch
            }
            if (newPass != confPass) {
                changePasswordStatus.value = "La nueva contraseña y su confirmación no coinciden"
                return@launch
            }
            if (newPass.length < 6) {
                changePasswordStatus.value = "La nueva contraseña debe tener al menos 6 caracteres"
                return@launch
            }

            val success = repository.changePassword(email, oldPass, newPass)
            if (success) {
                changePasswordStatus.value = "Contraseña cambiada exitosamente"
                // Check if changed user is current logged in
                val current = loggedInUser.value
                if (current != null && current.email.equals(email, ignoreCase = true)) {
                    val hashed = repository.hashPassword(newPass)
                    loggedInUser.value = current.copy(passwordHash = hashed)
                }
            } else {
                changePasswordStatus.value = "Correo o contraseña actual incorrecta"
            }
        }
    }

    fun forceResetPassword(email: String, newPass: String) {
        viewModelScope.launch {
            changePasswordStatus.value = null
            if (email.isBlank() || newPass.isBlank()) {
                changePasswordStatus.value = "El correo y contraseña nueva no pueden estar vacíos"
                return@launch
            }
            val success = repository.forceUpdatePassword(email, newPass)
            if (success) {
                changePasswordStatus.value = "Contraseña restablecida exitosamente"
            } else {
                changePasswordStatus.value = "El correo no se encuentra registrado"
            }
        }
    }

    fun logout() {
        loggedInUser.value = null
        loginSuccess.value = false
        loginError.value = null
        registerSuccess.value = null
        registerError.value = null
        changePasswordStatus.value = null
    }

    // Reactive Data Queries linked to loggedInUser
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<Category>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getCategories(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<ExpenseTransaction>> = combine(
        loggedInUser,
        selectedYear,
        selectedMonth
    ) { user, year, month ->
        Triple(user, year, month)
    }.flatMapLatest { (user, year, month) ->
        if (user == null) flowOf(emptyList())
        else repository.getTransactionsByPeriod(user.id, year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allRawTransactions: StateFlow<List<ExpenseTransaction>> = loggedInUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getTransactions(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedBudget: StateFlow<Budget?> = combine(
        loggedInUser,
        selectedYear,
        selectedMonth
    ) { user, year, month ->
        Triple(user, year, month)
    }.flatMapLatest { (user, year, month) ->
        if (user == null) flowOf(null)
        else repository.getBudgetFlow(user.id, year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Transaction Mutations
    fun addTransaction(amount: Double, currency: String, description: String, categoryId: Int, dateStr: String, isExpense: Boolean) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            // Parse Year and Month out of dateStr (dd/MM/yyyy)
            var year = Calendar.getInstance().get(Calendar.YEAR)
            var month = Calendar.getInstance().get(Calendar.MONTH) + 1
            try {
                val parts = dateStr.split("/")
                if (parts.size >= 3) {
                    dayOfWeekString = parts[0]
                    month = parts[1].toInt()
                    year = parts[2].toInt()
                }
            } catch (e: Exception) {
                // Keep default calendar values
            }

            val tx = ExpenseTransaction(
                userId = user.id,
                amount = amount,
                currency = currency,
                description = description,
                categoryId = categoryId,
                date = dateStr,
                year = year,
                month = month,
                isExpense = isExpense
            )
            repository.insertTransaction(tx)
        }
    }

    private var dayOfWeekString = ""

    fun deleteTransaction(tx: ExpenseTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun addCategory(name: String, isExpense: Boolean) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.insertCategory(user.id, name, isExpense)
        }
    }

    fun deleteCategory(cat: Category) {
        viewModelScope.launch {
            repository.deleteCategory(cat)
        }
    }

    fun setBudget(amount: Double) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.saveBudget(user.id, selectedYear.value, selectedMonth.value, amount)
        }
    }

    // Consolidated Calculations for Dashboard UI Stat Cards
    val dashboardStats = combine(
        transactions,
        selectedAnalysisCurrency
    ) { txList, analysisCurrency ->
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (tx in txList) {
            // Only aggregate transactions matching the selected analysis currency
            if (tx.currency != analysisCurrency) continue

            if (tx.isExpense) {
                totalExpense += tx.amount
            } else {
                totalIncome += tx.amount
            }
        }

        val balance = totalIncome - totalExpense
        Triple(balance, totalIncome, totalExpense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0.0, 0.0, 0.0))

    // Monthly data aggregator for Cash Flow comparison (Ingresos vs Egresos by month)
    // Returns List of monthly summaries for the select year, or last 6 months
    val monthlyFlowData = combine(
        allRawTransactions,
        selectedYear,
        selectedAnalysisCurrency
    ) { txList, currentYear, analysisCurrency ->
        val monthValuesMap = mutableMapOf<Int, Pair<Double, Double>>() // Month -> Pair(Income, Expense)
        
        // Filter by selected year and currency strictly
        val filtered = txList.filter { it.year == currentYear && it.currency == analysisCurrency }

        for (tx in filtered) {
            val currentPair = monthValuesMap[tx.month] ?: Pair(0.0, 0.0)
            if (tx.isExpense) {
                monthValuesMap[tx.month] = Pair(currentPair.first, currentPair.second + tx.amount)
            } else {
                monthValuesMap[tx.month] = Pair(currentPair.first + tx.amount, currentPair.second)
            }
        }
        
        monthValuesMap.toSortedMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Category Distribution aggregator
    val categoryDistributionData = combine(
        transactions,
        categories,
        selectedAnalysisCurrency
    ) { txList, categoriesList, analysisCurrency ->
        // Filter by expense and selected currency strictly
        val mapExpenses = txList.filter { it.isExpense && it.currency == analysisCurrency }
        val categoryTotals = mutableMapOf<Int, Double>()
        
        for (tx in mapExpenses) {
            categoryTotals[tx.categoryId] = (categoryTotals[tx.categoryId] ?: 0.0) + tx.amount
        }

        val namePercentList = mutableListOf<Pair<String, Double>>()
        val totalExpensesConverted = categoryTotals.values.sum()

        if (totalExpensesConverted > 0) {
            for ((catId, amt) in categoryTotals) {
                // Find category name
                val name = categoriesList.find { it.id == catId }?.name ?: "Otros"
                val percent = (amt / totalExpensesConverted) * 100
                namePercentList.add(Pair(name, percent))
            }
        }
        namePercentList.sortByDescending { it.second }
        namePercentList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
