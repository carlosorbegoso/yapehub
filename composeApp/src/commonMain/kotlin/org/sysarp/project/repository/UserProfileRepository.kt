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
    
    fun setCurrentUser(user: UserProfile) {
        currentUser = user
    }
    
    // Métodos de inicialización (solo para init)
    private fun addUser(user: UserProfile) {
        users.add(user)
    }
    
    private fun addStore(store: Store) {
        stores.add(store)
    }
}

