package org.sysarp.project.ui.common.components.filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.UserRole

/**
 * Panel de filtros moderno y expandible
 */
@Composable
fun ModernFiltersPanel(
    selectedStatuses: List<PaymentFilterStatus>,
    showAdvancedFilters: Boolean,
    startDate: String?,
    endDate: String?,
    userRole: UserRole,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit,
    onDateRangeChanged: (String?, String?) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onToggleAdvancedFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header del panel de filtros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtros Avanzados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = if (showAdvancedFilters) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (showAdvancedFilters) "Ocultar filtros" else "Mostrar filtros",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onToggleAdvancedFilters() }
                )
            }
            
            // Panel expandible
            AnimatedVisibility(
                visible = showAdvancedFilters,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Filtros de estado
                    ModernStatusFilter(
                        selectedStatuses = selectedStatuses,
                        userRole = userRole,
                        onStatusSelectionChanged = onStatusSelectionChanged
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Filtros de fecha
                    ModernDateRangeFilter(
                        startDate = startDate,
                        endDate = endDate,
                        onDateRangeChanged = onDateRangeChanged
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClearFilters,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Limpiar")
                        }
                        
                        Button(
                            onClick = onApplyFilters,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aplicar")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filtro de estados moderno
 */
@Composable
private fun ModernStatusFilter(
    selectedStatuses: List<PaymentFilterStatus>,
    userRole: UserRole,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Estados de Pago",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val availableStatuses = if (userRole == UserRole.ADMIN) {
            PaymentFilterStatus.values().toList()
        } else {
            PaymentFilterStatus.values().filter { it != PaymentFilterStatus.ALL }
        }
        
        if (userRole == UserRole.ADMIN) {
            // Radio buttons para admin (selección única)
            availableStatuses.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusSelectionChanged(listOf(status))
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedStatuses.contains(status),
                        onClick = {
                            onStatusSelectionChanged(listOf(status))
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getStatusDisplayName(status),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Checkboxes para vendedor (selección múltiple)
            availableStatuses.forEach { status ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val newSelection = if (selectedStatuses.contains(status)) {
                                selectedStatuses.filter { it != status }
                            } else {
                                selectedStatuses + status
                            }
                            onStatusSelectionChanged(newSelection)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedStatuses.contains(status),
                        onCheckedChange = { checked ->
                            val newSelection = if (checked) {
                                selectedStatuses + status
                            } else {
                                selectedStatuses.filter { it != status }
                            }
                            onStatusSelectionChanged(newSelection)
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getStatusDisplayName(status),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Filtro de rango de fechas moderno
 */
@Composable
private fun ModernDateRangeFilter(
    startDate: String?,
    endDate: String?,
    onDateRangeChanged: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Rango de Fechas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = startDate ?: "",
                onValueChange = { onDateRangeChanged(it.ifEmpty { null }, endDate) },
                label = { Text("Fecha Inicio") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = endDate ?: "",
                onValueChange = { onDateRangeChanged(startDate, it.ifEmpty { null }) },
                label = { Text("Fecha Fin") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Obtiene el nombre de visualización del estado
 */
private fun getStatusDisplayName(status: PaymentFilterStatus): String {
    return when (status) {
        PaymentFilterStatus.PENDING -> "Pendientes"
        PaymentFilterStatus.CLAIMED -> "Confirmados"
        PaymentFilterStatus.REJECTED -> "Rechazados"
        PaymentFilterStatus.ALL -> "Todos"
    }
}
