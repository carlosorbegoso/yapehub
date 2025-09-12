package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val assignedStores: List<String> = emptyList(), // Para vendedores, lista de tiendas asignadas
    val isActive: Boolean = true
)

@Serializable
enum class UserRole {
    ADMIN,      // Puede ver todas las transacciones y gestionar tiendas
    VENDOR      // Solo puede ver transacciones de sus tiendas asignadas
}

@Serializable
data class Store(
    val id: String,
    val name: String,
    val address: String? = null,
    val isActive: Boolean = true
)
