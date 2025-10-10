package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.formatCurrency

/**
 * Componente de filtros avanzados para la gestión de pagos administrativos
 * Incluye filtros por estado, vendedor, sucursal, monto, código Yape y nombre del cliente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFiltersComponent(
    payments: List<AdminPayment>,
    onFiltersChanged: (AdvancedFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(AdvancedFilters()) }
    
    // Obtener opciones únicas de los pagos con memoización optimizada
    val uniqueSellers by remember(payments) {
        derivedStateOf {
            payments.map { it.sellerName }
                .distinct()
                .sorted()
        }
    }
    
    val uniqueBranches by remember(payments) {
        derivedStateOf {
            payments.map { it.branchName }
                .distinct()
                .filterNotNull()
                .sorted()
        }
    }
    
    val uniqueStatuses by remember(payments) {
        derivedStateOf {
            payments.map { it.status }
                .distinct()
                .sorted()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con botón de expandir/colapsar
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
                        contentDescription = "Icono de filtros avanzados",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Filtros Avanzados",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contador de filtros activos
                    val activeFiltersCount = filters.getActiveFiltersCount()
                    if (activeFiltersCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$activeFiltersCount",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Colapsar filtros" else "Expandir filtros",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Contenido expandible
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Filtro por estado
                        StatusFilterSection(
                            statuses = uniqueStatuses,
                            selectedStatus = filters.status,
                            onStatusSelected = { status ->
                                filters = filters.copy(status = status)
                                onFiltersChanged(filters)
                            }
                        )
                        
                        // Filtro por vendedor
                        SellerFilterSection(
                            sellers = uniqueSellers,
                            selectedSellerName = filters.sellerName,
                            onSellerSelected = { sellerName ->
                                filters = filters.copy(sellerName = sellerName)
                                onFiltersChanged(filters)
                            }
                        )
                    
                    // Filtro por sucursal
                    BranchFilterSection(
                        branches = uniqueBranches,
                        selectedBranch = filters.branchName,
                        onBranchSelected = { branchName ->
                            filters = filters.copy(branchName = branchName)
                            onFiltersChanged(filters)
                        }
                    )
                    
                    // Filtro por rango de montos
                    AmountRangeFilterSection(
                        minAmount = filters.minAmount,
                        maxAmount = filters.maxAmount,
                        onAmountRangeChanged = { min, max ->
                            filters = filters.copy(minAmount = min, maxAmount = max)
                            onFiltersChanged(filters)
                        }
                    )
                    
                    // Filtro por código Yape
                    YapeCodeFilterSection(
                        yapeCode = filters.yapeCode,
                        onYapeCodeChanged = { code ->
                            filters = filters.copy(yapeCode = code)
                            onFiltersChanged(filters)
                        }
                    )
                    
                    // Filtro por nombre del cliente
                    CustomerNameFilterSection(
                        customerName = filters.customerName,
                        onCustomerNameChanged = { name ->
                            filters = filters.copy(customerName = name)
                            onFiltersChanged(filters)
                        }
                    )
                    
                    // Botones de acción mejorados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                filters = AdvancedFilters()
                                onFiltersChanged(filters)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = filters.hasActiveFilters()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resetear")
                        }
                        
                        Button(
                            onClick = { isExpanded = false },
                            modifier = Modifier.weight(1f),
                            enabled = filters.hasActiveFilters()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filtrar")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterSection(
    statuses: List<String>,
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Estado del Pago",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStatus ?: "Todos los estados",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos los estados") },
                    onClick = { onStatusSelected(null) },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = { onStatusSelected(status) },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerFilterSection(
    sellers: List<String>,
    selectedSellerName: String,
    onSellerSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Vendedor",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedSellerName.ifEmpty { "Todos los vendedores" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos los vendedores") },
                    onClick = { onSellerSelected("") },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                sellers.forEach { sellerName ->
                    DropdownMenuItem(
                        text = { Text(sellerName) },
                        onClick = { onSellerSelected(sellerName) },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchFilterSection(
    branches: List<String>,
    selectedBranch: String?,
    onBranchSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Sucursal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedBranch ?: "Todas las sucursales",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Todas las sucursales") },
                    onClick = { onBranchSelected(null) },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                branches.forEach { branch ->
                    DropdownMenuItem(
                        text = { Text(branch) },
                        onClick = { onBranchSelected(branch) },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountRangeFilterSection(
    minAmount: Double?,
    maxAmount: Double?,
    onAmountRangeChanged: (Double?, Double?) -> Unit
) {
    Column {
        Text(
            text = "Rango de Montos",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minAmount?.toString() ?: "",
                onValueChange = { value ->
                    val amount = value.toDoubleOrNull()
                    onAmountRangeChanged(amount, maxAmount)
                },
                label = { Text("Mínimo") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
                ),
                trailingIcon = {
                    if (minAmount != null) {
                        IconButton(
                            onClick = { onAmountRangeChanged(null, maxAmount) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Limpiar mínimo",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
            
            OutlinedTextField(
                value = maxAmount?.toString() ?: "",
                onValueChange = { value ->
                    val amount = value.toDoubleOrNull()
                    onAmountRangeChanged(minAmount, amount)
                },
                label = { Text("Máximo") },
                placeholder = { Text("1000.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
                ),
                trailingIcon = {
                    if (maxAmount != null) {
                        IconButton(
                            onClick = { onAmountRangeChanged(minAmount, null) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Limpiar máximo",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        }
        
        // Mostrar rango actual con validación
        if (minAmount != null || maxAmount != null) {
            Spacer(modifier = Modifier.height(4.dp))
            
            val isValidRange = minAmount == null || maxAmount == null || minAmount <= maxAmount
            val rangeText = if (isValidRange) {
                "Rango: ${formatCurrency(minAmount ?: 0.0)} - ${formatCurrency(maxAmount ?: Double.MAX_VALUE)}"
            } else {
                "⚠️ Rango inválido: El mínimo debe ser menor al máximo"
            }
            
            Text(
                text = rangeText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isValidRange) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun YapeCodeFilterSection(
    yapeCode: String,
    onYapeCodeChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Código Yape",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = yapeCode,
            onValueChange = onYapeCodeChanged,
            placeholder = { Text("Buscar por código Yape...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
            ),
            trailingIcon = {
                if (yapeCode.isNotEmpty()) {
                    IconButton(
                        onClick = { onYapeCodeChanged("") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Limpiar código",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun CustomerNameFilterSection(
    customerName: String,
    onCustomerNameChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Nombre del Cliente",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = customerName,
            onValueChange = onCustomerNameChanged,
            placeholder = { Text("Buscar por nombre del cliente...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                unfocusedContainerColor = androidx.compose.ui.graphics.Color.White
            ),
            trailingIcon = {
                if (customerName.isNotEmpty()) {
                    IconButton(
                        onClick = { onCustomerNameChanged("") }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Limpiar nombre",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

/**
 * Clase de datos para los filtros avanzados
 */
data class AdvancedFilters(
    val status: String? = null,
    val sellerName: String = "",
    val branchName: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val yapeCode: String = "",
    val customerName: String = ""
) {
    fun getActiveFiltersCount(): Int {
        var count = 0
        if (status != null) count++
        if (sellerName.isNotEmpty()) count++
        if (branchName != null) count++
        if (minAmount != null) count++
        if (maxAmount != null) count++
        if (yapeCode.isNotEmpty()) count++
        if (customerName.isNotEmpty()) count++
        return count
    }
    
    fun hasActiveFilters(): Boolean = getActiveFiltersCount() > 0
}
