package org.sysarp.project.ui.components.seller_unified.fields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.ui.components.ValidationErrorDisplay

/**
 * Componentes UI para SellerFormFields
 */

@Composable
fun SellerFormFieldsContainer(
    state: SellerFormFieldsState,
    onNavigateToQRScanner: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del formulario
            SellerFormFieldsHeader(state = state)
            
            // Campo de código de afiliación
            AffiliationCodeField(
                state = state,
                onNavigateToQRScanner = onNavigateToQRScanner
            )
            
            // Campo de nombre del vendedor
            SellerNameField(state = state)
            
            // Campo de teléfono
            PhoneField(state = state)
            
            // Botón de envío
            SubmitButton(
                state = state,
                onSubmit = onSubmit
            )
            
            // Mensaje de seguridad
            SecurityMessage()
            
            // Mensajes de error y éxito
            if (state.errorMessage.isNotEmpty()) {
                ValidationErrorDisplay(errorMessage = state.errorMessage)
            }
            
            if (state.successMessage.isNotEmpty()) {
                SuccessMessage(message = state.successMessage)
            }
        }
    }
}

@Composable
fun SellerFormFieldsHeader(
    state: SellerFormFieldsState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Datos de Acceso",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Indicador de campos completados
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val completedFields = state.getCompletedFieldsCount()
            Text(
                text = "$completedFields/3",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (state.areAllFieldsCompleted()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AffiliationCodeField(
    state: SellerFormFieldsState,
    onNavigateToQRScanner: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = state.affiliationCode,
            onValueChange = { newValue ->
                state.updateAffiliationCode(newValue)
            },
            label = { Text("Código de Afiliación") },
            placeholder = { Text("Ej: ABC123") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                when (state.getAffiliationCodeVisualState()) {
                    FieldVisualState.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Válido",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    FieldVisualState.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Muy corto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    FieldVisualState.Neutral -> { /* No icon */ }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isLoading,
            isError = state.hasAffiliationCodeError(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = when (state.getAffiliationCodeVisualState()) {
                    FieldVisualState.Valid -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    FieldVisualState.Error -> MaterialTheme.colorScheme.error
                    FieldVisualState.Neutral -> MaterialTheme.colorScheme.outline
                },
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        Text(
            text = "Código de 6 dígitos del administrador",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        
        OutlinedButton(
            onClick = onNavigateToQRScanner,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = !state.isLoading
        ) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Escanear Código QR",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SellerNameField(
    state: SellerFormFieldsState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = state.sellerName,
            onValueChange = { newValue ->
                state.updateSellerName(newValue)
            },
            label = { Text("Nombre Completo") },
            placeholder = { Text("Tu nombre completo") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                when (state.getSellerNameVisualState()) {
                    FieldVisualState.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Válido",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    FieldVisualState.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Muy corto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    FieldVisualState.Neutral -> { /* No icon */ }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isLoading,
            isError = state.hasSellerNameError(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = when (state.getSellerNameVisualState()) {
                    FieldVisualState.Valid -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    FieldVisualState.Error -> MaterialTheme.colorScheme.error
                    FieldVisualState.Neutral -> MaterialTheme.colorScheme.outline
                },
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        Text(
            text = "Nombre completo como en tu documento",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun PhoneField(
    state: SellerFormFieldsState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = state.phone,
            onValueChange = { newValue ->
                state.updatePhone(newValue)
            },
            label = { Text("Número de Teléfono") },
            placeholder = { Text("Ej: 987654321") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                when (state.getPhoneVisualState()) {
                    FieldVisualState.Valid -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Válido",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    FieldVisualState.Error -> {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Incompleto",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    FieldVisualState.Neutral -> { /* No icon */ }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.hasPhoneError(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = when (state.getPhoneVisualState()) {
                    FieldVisualState.Valid -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    FieldVisualState.Error -> MaterialTheme.colorScheme.error
                    FieldVisualState.Neutral -> MaterialTheme.colorScheme.outline
                },
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        Text(
            text = "Número de 9 dígitos (sin código de país)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun SubmitButton(
    state: SellerFormFieldsState,
    onSubmit: () -> Unit
) {
    Button(
        onClick = onSubmit,
        enabled = !state.isLoading && state.isFormValid(),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (state.isFormValid()) 4.dp else 0.dp,
            pressedElevation = if (state.isFormValid()) 8.dp else 0.dp
        )
    ) {
        if (state.isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Procesando...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Afiliarse",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SecurityMessage() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Tus datos están protegidos con validación estricta",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SuccessMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 14.sp
            )
        }
    }
}
