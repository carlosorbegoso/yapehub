package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para manejo de errores
@Serializable
data class ApiError(
    val message: String,
    val code: String,
    val details: ErrorDetails? = null,
    val timestamp: String
)

@Serializable
data class ErrorDetails(
    val validationErrors: Map<String, ValidationError>? = null,
    val field: String? = null,
    val reason: String? = null,
    val value: String? = null
)

@Serializable
data class ValidationError(
    val invalidValue: String? = null,
    val message: String
)
