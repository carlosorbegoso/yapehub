package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Int,        // Cambiado de totalItems
    val pageSize: Int,             // Cambiado de itemsPerPage
    val hasNext: Boolean,          // Nuevo campo
    val hasPrevious: Boolean,      // Nuevo campo
    val empty: Boolean,            // Nuevo campo
    val firstPage: Boolean,        // Nuevo campo
    val lastPage: Boolean          // Nuevo campo
)
