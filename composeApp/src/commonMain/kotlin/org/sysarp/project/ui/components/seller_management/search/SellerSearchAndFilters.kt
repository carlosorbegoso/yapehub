package org.sysarp.project.ui.components.seller_management.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SellerSearchAndFilters(
    showSearchBar: Boolean,
    showFilters: Boolean,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    filterActive: Boolean?,
    onFilterActiveChange: (Boolean?) -> Unit,
    filterOnline: Boolean?,
    onFilterOnlineChange: (Boolean?) -> Unit,
    sortBy: String,
    onSortByChange: (String) -> Unit,
    sortOrder: String,
    onSortOrderChange: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    // Barra de búsqueda
    if (showSearchBar) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Buscar vendedores...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange(TextFieldValue("")) }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
    
    // Filtros avanzados
    if (showFilters) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Filtros Avanzados",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Filtros de estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { onFilterActiveChange(null) },
                        label = { Text("Todos", fontSize = 12.sp) },
                        selected = filterActive == null,
                        leadingIcon = if (filterActive == null) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    FilterChip(
                        onClick = { onFilterActiveChange(true) },
                        label = { Text("Activos", fontSize = 12.sp) },
                        selected = filterActive == true,
                        leadingIcon = if (filterActive == true) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    FilterChip(
                        onClick = { onFilterActiveChange(false) },
                        label = { Text("Inactivos", fontSize = 12.sp) },
                        selected = filterActive == false,
                        leadingIcon = if (filterActive == false) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                // Filtros de conexión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        onClick = { onFilterOnlineChange(null) },
                        label = { Text("Todos", fontSize = 12.sp) },
                        selected = filterOnline == null,
                        leadingIcon = if (filterOnline == null) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    FilterChip(
                        onClick = { onFilterOnlineChange(true) },
                        label = { Text("En línea", fontSize = 12.sp) },
                        selected = filterOnline == true,
                        leadingIcon = if (filterOnline == true) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    FilterChip(
                        onClick = { onFilterOnlineChange(false) },
                        label = { Text("Desconectados", fontSize = 12.sp) },
                        selected = filterOnline == false,
                        leadingIcon = if (filterOnline == false) {
                            { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                // Opciones de ordenamiento
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ordenar por:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { 
                                onSortByChange("name")
                                onSortOrderChange(if (sortBy == "name" && sortOrder == "asc") "desc" else "asc")
                            },
                            label = { Text("Nombre", fontSize = 11.sp) },
                            selected = sortBy == "name",
                            leadingIcon = if (sortBy == "name") {
                                { 
                                    Icon(
                                        imageVector = if (sortOrder == "asc") Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        FilterChip(
                            onClick = { 
                                onSortByChange("payments")
                                onSortOrderChange(if (sortBy == "payments" && sortOrder == "asc") "desc" else "asc")
                            },
                            label = { Text("Pagos", fontSize = 11.sp) },
                            selected = sortBy == "payments",
                            leadingIcon = if (sortBy == "payments") {
                                { 
                                    Icon(
                                        imageVector = if (sortOrder == "asc") Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        FilterChip(
                            onClick = { 
                                onSortByChange("amount")
                                onSortOrderChange(if (sortBy == "amount" && sortOrder == "asc") "desc" else "asc")
                            },
                            label = { Text("Monto", fontSize = 11.sp) },
                            selected = sortBy == "amount",
                            leadingIcon = if (sortBy == "amount") {
                                { 
                                    Icon(
                                        imageVector = if (sortOrder == "asc") Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                
                // Botón para limpiar filtros
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Limpiar filtros",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar filtros", fontSize = 12.sp)
                }
            }
        }
    }
}
