package org.sysarp.project.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.data.BranchInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateAffiliationCodeDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (Int, Int, Int, String?) -> Unit,
    branches: List<BranchInfo> = emptyList(),
    isLoading: Boolean = false,
    generatedCode: AffiliationCodeData? = null,
    errorMessage: String? = null
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Generar Código de Afiliación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (generatedCode == null) {
                        // Formulario
                        var expirationHours by remember { mutableStateOf("2") }
                        var maxUses by remember { mutableStateOf("1") }
                        var selectedBranch by remember { mutableStateOf<BranchInfo?>(null) }
                        var notes by remember { mutableStateOf("") }
                        var showBranchDropdown by remember { mutableStateOf(false) }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Selector de sucursal
                            OutlinedTextField(
                                value = selectedBranch?.name ?: "",
                                onValueChange = { },
                                label = { Text("Sucursal") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Business,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showBranchDropdown = !showBranchDropdown }) {
                                        Icon(
                                            imageVector = if (showBranchDropdown) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null
                                        )
                                    }
                                },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = selectedBranch == null && showBranchDropdown
                            )
                            
                            // Dropdown de sucursales
                            if (showBranchDropdown) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp)
                                    ) {
                                        items(branches) { branch ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { 
                                                        selectedBranch = branch
                                                        showBranchDropdown = false
                                                    }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Business,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = branch.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Código: ${branch.code}",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Horas de expiración
                            OutlinedTextField(
                                value = expirationHours,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                        expirationHours = newValue
                                    }
                                },
                                label = { Text("Horas de expiración") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = expirationHours.toIntOrNull()?.let { it < 1 || it > 24 } ?: false
                            )
                            
                            // Máximo de usos
                            OutlinedTextField(
                                value = maxUses,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                        maxUses = newValue
                                    }
                                },
                                label = { Text("Máximo de usos") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                isError = maxUses.toIntOrNull()?.let { it < 1 || it > 100 } ?: false
                            )
                            
                            // Notas
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notas (opcional)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Note,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3
                            )
                            
                            // Mensaje de error
                            if (errorMessage != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = errorMessage,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar")
                            }
                            
                            Button(
                                onClick = {
                                    if (selectedBranch != null) {
                                        onGenerate(
                                            expirationHours.toIntOrNull() ?: 2,
                                            maxUses.toIntOrNull() ?: 1,
                                            selectedBranch!!.branchId,
                                            notes.ifBlank { null }
                                        )
                                    }
                                },
                                enabled = !isLoading && selectedBranch != null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isLoading) "Generando..." else "Generar Código")
                            }
                        }
                    } else {
                        // Código generado
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Código Generado",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = generatedCode.affiliationCode,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Expira: ${generatedCode.expiresAt}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Text(
                                    text = "Usos restantes: ${generatedCode.remainingUses}/${generatedCode.maxUses}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}
