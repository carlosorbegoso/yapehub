package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)
