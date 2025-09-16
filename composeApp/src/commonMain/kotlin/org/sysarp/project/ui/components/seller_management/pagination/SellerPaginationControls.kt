package org.sysarp.project.ui.components.seller_management.pagination

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SellerPaginationControls(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    onPageChange: (Int) -> Unit,
    onFirstPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onLastPage: () -> Unit
) {
    if (totalPages > 1) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Información de paginación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Página $currentPage de $totalPages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "$totalItems vendedores",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Controles de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primera página
                    OutlinedButton(
                        onClick = onFirstPage,
                        enabled = currentPage > 1,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FirstPage,
                            contentDescription = "Primera página",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Página anterior
                    OutlinedButton(
                        onClick = onPreviousPage,
                        enabled = currentPage > 1,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Página anterior",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Números de página (mostrar páginas cercanas)
                    val startPage = maxOf(1, currentPage - 2)
                    val endPage = minOf(totalPages, currentPage + 2)
                    
                    for (page in startPage..endPage) {
                        if (page == currentPage) {
                            Button(
                                onClick = { },
                                modifier = Modifier.size(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = page.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onPageChange(page) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text(
                                    text = page.toString(),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    
                    // Página siguiente
                    OutlinedButton(
                        onClick = onNextPage,
                        enabled = currentPage < totalPages,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Página siguiente",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Última página
                    OutlinedButton(
                        onClick = onLastPage,
                        enabled = currentPage < totalPages,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LastPage,
                            contentDescription = "Última página",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
