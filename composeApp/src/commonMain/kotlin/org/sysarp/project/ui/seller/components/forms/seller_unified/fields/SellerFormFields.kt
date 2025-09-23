package org.sysarp.project.ui.components.seller_unified.fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.ui.components.ValidationErrorDisplay

@Composable
fun SellerFormFields(
    affiliationCode: String,
    sellerName: String,
    phone: String,
    isLoading: Boolean,
    errorMessage: String,
    successMessage: String,
    isFormValid: Boolean,
    onAffiliationCodeChange: (String) -> Unit,
    onSellerNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
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
                    val completedFields = listOf(affiliationCode.isNotBlank(), sellerName.isNotBlank(), phone.isNotBlank()).count { it }
                    Text(
                        text = "$completedFields/3",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (completedFields == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Campo de código de afiliación
            AffiliationCodeField(
                value = affiliationCode,
                onValueChange = onAffiliationCodeChange,
                isLoading = isLoading,
                onNavigateToQRScanner = onNavigateToQRScanner
            )
            
            // Campo de nombre del vendedor
            SellerNameField(
                value = sellerName,
                onValueChange = onSellerNameChange,
                isLoading = isLoading
            )
            
            // Campo de teléfono
            PhoneField(
                value = phone,
                onValueChange = onPhoneChange,
                isLoading = isLoading
            )
            
            // Botón de envío
            SubmitButton(
                isLoading = isLoading,
                isFormValid = isFormValid,
                onSubmit = onSubmit
            )
            
            // Mensaje de seguridad
            SecurityMessage()
            
            // Mensajes de error y éxito
            if (errorMessage.isNotEmpty()) {
                ValidationErrorDisplay(errorMessage = errorMessage)
            }
            
            if (successMessage.isNotEmpty()) {
                SuccessMessage(message = successMessage)
            }
        }
    }
}

@Composable
private fun AffiliationCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    onNavigateToQRScanner: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val sanitizedValue = newValue.filter { it.isLetterOrDigit() }.take(10)
                onValueChange(sanitizedValue)
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
                if (value.isNotBlank() && value.length >= 6) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (value.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Muy corto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            isError = value.isNotBlank() && value.length < 6,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (value.isNotBlank() && value.length >= 6) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
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
            enabled = !isLoading
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
private fun SellerNameField(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val sanitizedValue = newValue.filter { it.isLetter() || it == ' ' || it == '-' }.take(50)
                onValueChange(sanitizedValue)
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
                if (value.isNotBlank() && value.length >= 3) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (value.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Muy corto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            isError = value.isNotBlank() && value.length < 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (value.isNotBlank() && value.length >= 3) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
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
private fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val sanitizedValue = newValue.filter { it.isDigit() }.take(9)
                onValueChange(sanitizedValue)
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
                if (value.isNotBlank() && value.length == 9) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Válido",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (value.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Incompleto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = value.isNotBlank() && value.length != 9,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (value.isNotBlank() && value.length == 9) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
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
private fun SubmitButton(
    isLoading: Boolean,
    isFormValid: Boolean,
    onSubmit: () -> Unit
) {
    Button(
        onClick = onSubmit,
        enabled = !isLoading && isFormValid,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFormValid) 4.dp else 0.dp,
            pressedElevation = if (isFormValid) 8.dp else 0.dp
        )
    ) {
        if (isLoading) {
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
private fun SecurityMessage() {
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
private fun SuccessMessage(message: String) {
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
