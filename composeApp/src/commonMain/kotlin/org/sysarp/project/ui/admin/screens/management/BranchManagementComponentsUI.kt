package org.sysarp.project.ui.admin.screens.management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.BranchInfo

/**
 * Componentes UI para BranchManagementComponents
 */

@Composable
fun BranchManagementHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Gestión de Sucursales",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Administra tus sucursales y equipos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BranchStatsSection(
    state: BranchManagementComponentsState
) {
    // Título de la sección
    Text(
        text = "Estadísticas Principales",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    // Contenido de estadísticas
    if (state.isCurrentlyLoading()) {
        BranchLoadingCard()
    } else if (state.hasError()) {
        BranchErrorCard(
            title = "Error cargando sucursales",
            message = state.getCurrentErrorMessage() ?: "Error desconocido"
        )
    } else if (state.hasBranchStats()) {
        val branchStats = state.getCurrentBranchStats()!!
        val stats = listOf(
            BranchStat(
                title = "Total",
                value = branchStats.total.toString(),
                icon = Icons.Filled.Business,
                color = MaterialTheme.colorScheme.primary,
                trend = "+${branchStats.total}"
            ),
            BranchStat(
                title = "Activas",
                value = branchStats.active.toString(),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                trend = "+${branchStats.active}"
            ),
            BranchStat(
                title = "Inactivas",
                value = branchStats.inactive.toString(),
                icon = Icons.Filled.PauseCircle,
                color = MaterialTheme.colorScheme.error,
                trend = "+${branchStats.inactive}"
            ),
            BranchStat(
                title = "Vendedores",
                value = branchStats.totalSellers.toString(),
                icon = Icons.Filled.People,
                color = MaterialTheme.colorScheme.secondary,
                trend = "+${branchStats.totalSellers}"
            )
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(stats) { stat ->
                BranchStatCard(
                    title = stat.title,
                    value = stat.value,
                    icon = stat.icon,
                    color = stat.color,
                    trend = stat.trend
                )
            }
        }
    }
}

@Composable
fun BranchFiltersSection(
    state: BranchManagementComponentsState,
    onFilterChange: (String?) -> Unit
) {
    if (state.areFiltersVisible()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Filtros rápidos
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todas", "Activas", "Inactivas").forEach { filter ->
                        FilterChip(
                            onClick = { 
                                onFilterChange(when (filter) {
                                    "Todas" -> null
                                    "Activas" -> "active"
                                    "Inactivas" -> "inactive"
                                    else -> null
                                })
                            },
                            label = { Text(filter) },
                            selected = when (filter) {
                                "Todas" -> !state.hasActiveFilter()
                                "Activas" -> state.getCurrentFilterStatus() == "active"
                                "Inactivas" -> state.getCurrentFilterStatus() == "inactive"
                                else -> false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (filter) {
                                        "Todas" -> Icons.Filled.Business
                                        "Activas" -> Icons.Filled.CheckCircle
                                        "Inactivas" -> Icons.Filled.PauseCircle
                                        else -> Icons.Filled.Business
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BranchListSection(
    state: BranchManagementComponentsState,
    onEdit: (BranchInfo) -> Unit,
    onViewSellers: (BranchInfo) -> Unit,
    onToggleStatus: (BranchInfo) -> Unit,
    onDelete: (BranchInfo) -> Unit,
    onViewDetails: (BranchInfo) -> Unit
) {
    // Título de la sección
    Text(
        text = "Lista de Sucursales",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    // Contenido de la lista
    if (!state.hasBranches() && !state.isCurrentlyLoading()) {
        EmptyBranchesCard()
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.getFilteredBranches().forEach { branch ->
                BranchCardV2(
                    branch = branch,
                    onEdit = { onEdit(branch) },
                    onViewSellers = { onViewSellers(branch) },
                    onToggleStatus = { onToggleStatus(branch) },
                    onDelete = { onDelete(branch) },
                    onViewDetails = { onViewDetails(branch) }
                )
            }
        }
    }
}

@Composable
fun BranchPaginationSection(
    state: BranchManagementComponentsState,
    onPageChange: (Int) -> Unit
) {
    if (state.getTotalPagesCount() > 1) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Página ${state.getCurrentPageNumber()} de ${state.getTotalPagesCount()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            if (state.getCurrentPageNumber() > 1) 
                                onPageChange(state.getCurrentPageNumber() - 1) 
                        },
                        enabled = state.getCurrentPageNumber() > 1
                    ) {
                        Text("Anterior")
                    }
                    
                    Button(
                        onClick = { 
                            if (state.getCurrentPageNumber() < state.getTotalPagesCount()) 
                                onPageChange(state.getCurrentPageNumber() + 1) 
                        },
                        enabled = state.getCurrentPageNumber() < state.getTotalPagesCount()
                    ) {
                        Text("Siguiente")
                    }
                }
            }
        }
    }
}

@Composable
fun BranchStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    trend: String
) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = trend,
                style = MaterialTheme.typography.bodySmall,
                color = if (trend.startsWith("+")) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BranchCardV2(
    branch: BranchInfo,
    onEdit: () -> Unit,
    onViewSellers: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header con información principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de sucursal
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (branch.isActive) 
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = if (branch.isActive) 
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información de la sucursal
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (branch.isActive) 
                                Icons.Filled.CheckCircle 
                            else Icons.Filled.PauseCircle,
                            contentDescription = null,
                            tint = if (branch.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (branch.isActive) "Activa" else "Inactiva",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (branch.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Acciones principales - Diseño moderno con iconos grandes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón de detalles
                ActionButton(
                    icon = Icons.Filled.Info,
                    label = "Detalles",
                    onClick = onViewDetails,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Botón de vendedores
                ActionButton(
                    icon = Icons.Filled.People,
                    label = "Vendedores",
                    onClick = onViewSellers,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                // Botón de editar
                ActionButton(
                    icon = Icons.Filled.Edit,
                    label = "Editar",
                    onClick = onEdit,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Acción de estado - Botón destacado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onToggleStatus,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (branch.isActive) 
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primary,
                        contentColor = if (branch.isActive) 
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = if (branch.isActive) 
                            Icons.Filled.Pause 
                        else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (branch.isActive) "Pausar Sucursal" else "Activar Sucursal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BranchLoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Cargando sucursales...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun BranchErrorCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun EmptyBranchesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = "No hay sucursales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "No se encontraron sucursales con los filtros aplicados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Botón de acción personalizado con diseño moderno
 */
@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
