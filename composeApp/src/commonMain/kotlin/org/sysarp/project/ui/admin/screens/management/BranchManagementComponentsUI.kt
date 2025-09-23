package org.sysarp.project.ui.admin.screens.management

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

/**
 * Header usando TopBarComponent para la pantalla de gestión de sucursales
 */
@Composable
fun BranchManagementHeader(
    onNavigateBack: (() -> Unit)? = null
) {
    TopBarComponent(
        title = "Gestión de Sucursales",
        subtitle = "Administra tus sucursales y equipos",
        icon = Icons.Filled.Business,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun BranchStatsSection(
    state: BranchManagementComponentsState
) {
    if (state.isCurrentlyLoading()) {
        BranchLoadingCard()
    } else if (state.hasError()) {
        BranchErrorCard(
            title = "Error cargando sucursales",
            message = state.getCurrentErrorMessage() ?: "Error desconocido"
        )
    } else if (state.hasBranchStats()) {
        val branchStats = state.getCurrentBranchStats()!!
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grid de estadísticas moderno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total de sucursales
                ModernStatCard(
                    title = "Total",
                    value = branchStats.total.toString(),
                    icon = Icons.Filled.Business,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                // Sucursales activas
                ModernStatCard(
                    title = "Activas",
                    value = branchStats.active.toString(),
                    icon = Icons.Filled.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vendedores totales
                ModernStatCard(
                    title = "Vendedores",
                    value = branchStats.totalSellers.toString(),
                    icon = Icons.Filled.People,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                
                // Promedio por sucursal
                val avgSellers = if (branchStats.total > 0) 
                    (branchStats.totalSellers / branchStats.total) else 0
                ModernStatCard(
                    title = "Promedio",
                    value = avgSellers.toString(),
                    icon = Icons.Filled.Info,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
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
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filtros modernos con chips
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
                        label = { 
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
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
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
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
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Contenido de la lista
        if (!state.hasBranches() && !state.isCurrentlyLoading()) {
            EmptyBranchesCard()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
fun ModernStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado circular (como en la lista de tareas)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (branch.isActive) 
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (branch.isActive) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Operativa",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PauseCircle,
                        contentDescription = "Inactiva",
                        tint = MaterialTheme.colorScheme.outline,
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = branch.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Estado con estilo minimalista
                Text(
                    text = if (branch.isActive) "Operativa" else "Inactiva",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (branch.isActive) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Acciones en el lado derecho (como en la lista de tareas)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de eliminar (icono de papelera)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Botón de expandir/ver detalles
                IconButton(
                    onClick = onViewDetails,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = "Ver detalles",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        // Acciones secundarias (aparecen al expandir)
        if (false) { // TODO: Implementar estado de expansión
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón de vendedores
                    ModernActionButton(
                        icon = Icons.Filled.People,
                        label = "Vendedores",
                        onClick = onViewSellers,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Botón de editar
                    ModernActionButton(
                        icon = Icons.Filled.Edit,
                        label = "Editar",
                        onClick = onEdit,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.weight(1f)
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
 * Botón de acción moderno y limpio
 */
@Composable
fun ModernActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

/**
 * Botón flotante para agregar nueva sucursal (inspirado en la lista de tareas)
 */
@Composable
fun AddBranchFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Agregar sucursal",
            modifier = Modifier.size(24.dp)
        )
    }
}

