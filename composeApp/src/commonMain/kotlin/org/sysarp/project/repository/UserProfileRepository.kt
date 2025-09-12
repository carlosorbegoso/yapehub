package org.sysarp.project.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole

class UserProfileRepository {
    
    // Datos de configuración real
    private val stores = mutableListOf<Store>()
    private val users = mutableListOf<UserProfile>()
    
    init {
        // Inicializar con datos básicos
        addStore(Store("1", "Negocio Principal", "Ubicación principal"))
        
        // Crear usuario administrador por defecto para poder usar la app
        addUser(UserProfile(
            id = "admin",
            name = "Administrador",
            email = "admin@yapechamo.com",
            role = UserRole.ADMIN,
            assignedStores = listOf("1")
        ))
    }
    
    private var currentUser: UserProfile? = null
    
    fun getAllUsers(): Flow<List<UserProfile>> {
        return flowOf(users.toList())
    }
    
    fun getAllStores(): Flow<List<Store>> {
        return flowOf(stores.toList())
    }
    
    fun getCurrentUser(): UserProfile? {
        return currentUser
    }
    
    fun setCurrentUser(user: UserProfile) {
        currentUser = user
    }
    
    fun login(email: String, password: String): UserProfile? {
        // En una implementación real, aquí se validarían las credenciales
        return users.find { it.email == email }
    }
    
    fun getStoresForUser(userId: String): Flow<List<Store>> {
        val user = users.find { it.id == userId }
        val storeIds = user?.assignedStores ?: emptyList()
        val userStores = stores.filter { it.id in storeIds }
        return flowOf(userStores)
    }
    
    fun canUserAccessStore(userId: String, storeId: String): Boolean {
        val user = users.find { it.id == userId }
        return when (user?.role) {
            UserRole.ADMIN -> true
            UserRole.VENDOR -> storeId in (user.assignedStores ?: emptyList())
            null -> false
        }
    }
    
    fun addUser(user: UserProfile) {
        users.add(user)
    }
    
    fun updateUser(user: UserProfile) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }
    
    fun deleteUser(userId: String) {
        users.removeAll { it.id == userId }
    }
    
    fun addStore(store: Store) {
        stores.add(store)
    }
    
    fun updateStore(store: Store) {
        val index = stores.indexOfFirst { it.id == store.id }
        if (index != -1) {
            stores[index] = store
        }
    }
    
    fun deleteStore(storeId: String) {
        stores.removeAll { it.id == storeId }
    }
}
