package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.local.GymDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.GymRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GymDatabase.getInstance(application)
    val gymRepository = GymRepository(db)
    val authRepository = AuthRepository(application)
    private val backupManager = BackupManager(application, db)

    // Auth State
    val isLoggedIn: StateFlow<Boolean> = authRepository.token.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.isLoggedIn())

    val userName: StateFlow<String> = authRepository.userName

    // Theme State
    val isDarkMode = MutableStateFlow<Boolean?>(null)

    fun toggleTheme(systemIsDark: Boolean) {
        val current = isDarkMode.value ?: systemIsDark
        isDarkMode.value = !current
    }

    // Navigation & Selected Client (for Tablet Split-View or Phone details)
    private val _selectedClientId = MutableStateFlow<Long?>(1L)
    val selectedClientId: StateFlow<Long?> = _selectedClientId.asStateFlow()

    // Client Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedFilterCategories = MutableStateFlow<Set<String>>(emptySet()) // "visits", "days", "expiring"

    val clients: StateFlow<List<Client>> = combine(
        searchQuery,
        selectedFilterCategories,
        gymRepository.searchClients(""),
    ) { query, categories, allClients ->
        var list = if (query.isBlank()) {
            allClients
        } else {
            allClients.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.clientCode.contains(query, ignoreCase = true)
            }
        }

        if (categories.isNotEmpty() && !categories.contains("all")) {
            list = list.filter { client ->
                val active = client.activeMembership
                val matchesVisits = (categories.contains("visits") && active?.durationType == "visits")
                val matchesDays = (categories.contains("days") && active?.durationType == "days")
                val matchesExpiring = categories.contains("expiring") && (active?.isExpired == true || (active?.visitsLeft ?: 99) <= 2)
                matchesVisits || matchesDays || matchesExpiring
            }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClient: StateFlow<Client?> = _selectedClientId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else gymRepository.getClientByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Client Sub-lists for Client Detail Screen
    val selectedClientVisits: StateFlow<List<Visit>> = _selectedClientId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else gymRepository.getClientVisits(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientPurchases: StateFlow<List<MembershipPurchase>> = _selectedClientId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else gymRepository.getClientPurchases(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientSales: StateFlow<List<ProductSale>> = _selectedClientId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else gymRepository.getClientSales(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Products
    val productCategoryFilters = MutableStateFlow<Set<String>>(emptySet())
    val productSearchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>> = combine(productCategoryFilters, productSearchQuery) { cats, q ->
        Pair(cats, q)
    }.flatMapLatest { (cats, q) ->
        gymRepository.getProductsAdmin(cats, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Membership Types
    val membershipTypes: StateFlow<List<MembershipType>> = gymRepository.getMembershipTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val membershipTypesAdmin: StateFlow<List<MembershipType>> = gymRepository.getMembershipTypesAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History Timeline
    val historyTypeFilters = MutableStateFlow<Set<String>>(emptySet())
    val historyDateFilter = MutableStateFlow("today")

    val historyEvents: StateFlow<List<HistoryEvent>> = combine(historyTypeFilters, historyDateFilter) { types, date ->
        Pair(types, date)
    }.flatMapLatest { (types, date) ->
        gymRepository.getHistoryEvents(types, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Stats & Reports with Date Range Filter
    val analyticsDateFilter = MutableStateFlow("today") // "today", "yesterday", "week", "month", "all", "custom|...|..."

    val dashboardStats: StateFlow<DashboardStats> = analyticsDateFilter
        .flatMapLatest { filter -> gymRepository.getDashboardStatsFiltered(filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats(0.0, 0, 0, 0, 0.0, 0.0))

    val expiringMemberships: StateFlow<List<ExpiringMembershipInfo>> = gymRepository.getExpiringMemberships(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Feedback Message Toast/Snackbar
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            gymRepository.ensureDataInitialized()
        }
    }

    fun selectClient(id: Long) {
        _selectedClientId.value = id
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            val result = authRepository.loginOnline(email, pass)
            result.onSuccess { name ->
                _uiMessage.emit("Добро пожаловать в GymTrack, $name!")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Ошибка входа")
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // Key Action: -1 Visit Check-in
    fun deductVisit(clientId: Long) {
        viewModelScope.launch {
            val result = gymRepository.deductVisit(clientId)
            result.onSuccess { msg ->
                _uiMessage.emit(msg)
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Ошибка списывания визита")
            }
        }
    }
    
    fun cancelCheckIn(visitId: Long) {
        viewModelScope.launch {
            val result = gymRepository.cancelCheckIn(visitId)
            result.onSuccess {
                _uiMessage.emit("Визит отменен, количество посещений возвращено!")
            }.onFailure { err ->
                _uiMessage.emit(err.message ?: "Ошибка отмены визита")
            }
        }
    }

    fun addClient(fullName: String, phone: String, note: String?) {
        viewModelScope.launch {
            if (fullName.isBlank() || phone.isBlank()) {
                _uiMessage.emit("Заполните ФИО и номер телефона")
                return@launch
            }
            val result = gymRepository.addClient(fullName, phone, note)
            result.onSuccess { newClient ->
                _selectedClientId.value = newClient.id
                _uiMessage.emit("Клиент ${newClient.fullName} создан (${newClient.clientCode})")
            }.onFailure {
                _uiMessage.emit("Ошибка создания клиента")
            }
        }
    }

    fun deleteClient(id: Long) {
        viewModelScope.launch {
            val result = gymRepository.deleteClient(id)
            result.onSuccess {
                _uiMessage.emit("Клиент удален")
                _selectedClientId.value = null
            }.onFailure {
                _uiMessage.emit("Ошибка удаления клиента")
            }
        }
    }

    fun purchaseMembership(clientId: Long, typeId: Long, paymentMethod: String) {
        viewModelScope.launch {
            val result = gymRepository.purchaseMembership(clientId, typeId, paymentMethod)
            result.onSuccess {
                _uiMessage.emit("Абонемент успешно оформлен!")
            }.onFailure {
                _uiMessage.emit("Ошибка оформления абонемента")
            }
        }
    }
    
    fun deleteMembershipType(id: Long) {
        viewModelScope.launch {
            val result = gymRepository.deleteMembershipType(id)
            result.onSuccess { _uiMessage.emit("Тариф удален") }
            result.onFailure { _uiMessage.emit(it.message ?: "Ошибка") }
        }
    }

    fun updateMembershipType(id: Long, name: String, durationType: String, durationValue: Int, price: Double) {
        viewModelScope.launch {
            val result = gymRepository.updateMembershipType(id, name, durationType, durationValue, price)
            result.onSuccess { _uiMessage.emit("Тариф успешно обновлен") }
            result.onFailure { _uiMessage.emit(it.message ?: "Ошибка") }
        }
    }

    fun addMembershipType(name: String, durationType: String, durationValue: Int, price: Double) {
        viewModelScope.launch {
            val result = gymRepository.addMembershipType(name, durationType, durationValue, price)
            result.onSuccess { _uiMessage.emit("Тариф создан") }
                .onFailure { _uiMessage.emit(it.message ?: "Ошибка") }
        }
    }

    fun sellProduct(clientId: Long, productId: Long, quantity: Int, paymentMethod: String) {
        viewModelScope.launch {
            val result = gymRepository.sellProduct(clientId, productId, quantity, paymentMethod)
            result.onSuccess {
                _uiMessage.emit("Продажа товара зафиксирована!")
            }.onFailure {
                _uiMessage.emit(it.message ?: "Ошибка продажи")
            }
        }
    }

    fun addProduct(name: String, category: String, price: Double, stock: Int) {
        viewModelScope.launch {
            if (name.isBlank() || price <= 0) {
                _uiMessage.emit("Введите правильное название и цену")
                return@launch
            }
            val result = gymRepository.addProduct(name, category, price, stock)
            result.onSuccess {
                _uiMessage.emit("Товар '$name' добавлен в каталог")
            }.onFailure {
                _uiMessage.emit("Ошибка добавления товара")
            }
        }
    }
    
    fun updateProduct(id: Long, name: String, category: String, price: Double, stock: Int) {
        viewModelScope.launch {
            if (name.isBlank() || price <= 0) {
                _uiMessage.emit("Введите правильное название и цену")
                return@launch
            }
            val result = gymRepository.updateProduct(id, name, category, price, stock)
            result.onSuccess { _uiMessage.emit("Товар успешно обновлен") }
            result.onFailure { _uiMessage.emit(it.message ?: "Ошибка") }
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            val result = gymRepository.deleteProduct(id)
            result.onSuccess { _uiMessage.emit("Товар удален") }
            result.onFailure { _uiMessage.emit(it.message ?: "Ошибка") }
        }
    }

    // Currencies
    val currencies: StateFlow<List<Currency>> = gymRepository.getAllCurrencies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCurrency: StateFlow<Currency?> = gymRepository.getSelectedCurrency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addCurrency(name: String, code: String) {
        viewModelScope.launch {
            if (name.isNotBlank() && code.isNotBlank()) {
                gymRepository.addCurrency(name, code)
            }
        }
    }

    fun selectCurrency(id: Long) {
        viewModelScope.launch {
            gymRepository.selectCurrency(id)
        }
    }

    fun deleteCurrency(currency: Currency) {
        viewModelScope.launch {
            if (currency.isSelected) return@launch
            gymRepository.deleteCurrency(currency.id, currency.name, currency.code, currency.isSelected)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            db.clientDao().deleteAll()
            db.visitDao().deleteAll()
            db.membershipDao().deleteAllPurchases()
            db.saleDao().deleteAll()
            _selectedClientId.value = null
            _uiMessage.emit("Данные успешно удалены!")
        }
    }

    // Backup & Restore
    val backupStatus = MutableStateFlow<String?>(null)

    fun exportBackup() {
        viewModelScope.launch {
            backupStatus.value = "Экспорт..."
            val result = backupManager.exportBackup()
            backupStatus.value = result.fold(
                onSuccess = { fileName -> 
                    launch { _uiMessage.emit("Резервная копия успешно сохранена: $fileName") }
                    "✓ Сохранено: $fileName" 
                },
                onFailure = { 
                    launch { _uiMessage.emit("Ошибка экспорта: ${it.message}") }
                    "Ошибка: ${it.message}" 
                }
            )
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            backupStatus.value = "Импорт..."
            val result = backupManager.importBackup(uri)
            _selectedClientId.value = null
            backupStatus.value = result.fold(
                onSuccess = { msg -> 
                    launch { _uiMessage.emit("Данные успешно импортированы!") }
                    "✓ $msg" 
                },
                onFailure = { 
                    launch { _uiMessage.emit("Ошибка импорта: ${it.message}") }
                    "Ошибка: ${it.message}" 
                }
            )
        }
    }

    fun clearBackupStatus() {
        backupStatus.value = null
    }
}
