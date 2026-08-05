package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GymDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.GymRepository
import com.example.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val db = GymDatabase.getInstance(application)
    val gymRepository = GymRepository(db)
    val authRepository = AuthRepository(application)

    // Auth State
    val isLoggedIn: StateFlow<Boolean> = authRepository.token.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.isLoggedIn())

    val userName: StateFlow<String> = authRepository.userName

    // Navigation & Selected Client (for Tablet Split-View or Phone details)
    private val _selectedClientId = MutableStateFlow<Long?>(1L)
    val selectedClientId: StateFlow<Long?> = _selectedClientId.asStateFlow()

    // Client Search & Filter
    val searchQuery = MutableStateFlow("")
    val selectedFilterCategory = MutableStateFlow("all") // "all", "visits", "days", "expiring"

    val clients: StateFlow<List<Client>> = combine(
        searchQuery,
        selectedFilterCategory,
        gymRepository.searchClients("")
    ) { query, category, allClients ->
        var list = if (query.isBlank()) {
            allClients
        } else {
            allClients.filter {
                it.fullName.contains(query, ignoreCase = true) ||
                        it.phone.contains(query) ||
                        it.clientCode.contains(query, ignoreCase = true)
            }
        }

        when (category) {
            "visits" -> list = list.filter { it.activeMembership?.durationType == "visits" }
            "days" -> list = list.filter { it.activeMembership?.durationType == "days" }
            "expiring" -> list = list.filter { it.activeMembership?.isExpired == true || (it.activeMembership?.visitsLeft ?: 99) <= 2 }
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
    val productCategoryFilter = MutableStateFlow("all")
    val productSearchQuery = MutableStateFlow("")

    val products: StateFlow<List<Product>> = combine(productCategoryFilter, productSearchQuery) { cat, q ->
        Pair(cat, q)
    }.flatMapLatest { (cat, q) ->
        gymRepository.getProducts(cat, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Membership Types
    val membershipTypes: StateFlow<List<MembershipType>> = gymRepository.getMembershipTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History Timeline
    val historyTypeFilter = MutableStateFlow("all")
    val historyEvents: StateFlow<List<HistoryEvent>> = historyTypeFilter
        .flatMapLatest { gymRepository.getHistoryEvents(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Stats & Reports
    val dashboardStats: StateFlow<DashboardStats> = gymRepository.getDashboardStats()
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
            authRepository.setAuth("token-${System.currentTimeMillis()}", if (email.contains("@")) email.substringBefore("@") else "Менеджер")
            _uiMessage.emit("Добро пожаловать в GymTrack!")
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

}
