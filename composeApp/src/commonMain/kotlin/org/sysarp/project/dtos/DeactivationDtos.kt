package org.sysarp.project.dtos

import kotlinx.serialization.Serializable

// DTOs para el sistema de desactivación de vendedores

@Serializable
data class DeactivationRequest(
    val id: String,
    val sellerId: String,
    val sellerName: String,
    val adminId: String,
    val reason: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val requestedAt: String,
    val processedAt: String? = null,
    val processedBy: String? = null,
    val adminNotes: String? = null
)

@Serializable
data class DeactivationResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class CreateDeactivationRequest(
    val sellerId: String,
    val reason: String
)

@Serializable
data class ProcessDeactivationRequest(
    val requestId: String,
    val action: String, // APPROVE, REJECT
    val adminNotes: String? = null
)
