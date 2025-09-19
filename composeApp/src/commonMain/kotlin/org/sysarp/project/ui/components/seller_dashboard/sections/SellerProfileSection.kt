package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.components.cards.SellerProfileCard

/**
 * Sección del perfil del vendedor en el dashboard
 * Muestra información del vendedor y estado de conexión
 */
@Composable
fun SellerProfileSection(
    userProfile: UserProfile?,
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(top = 64.dp)
    ) {
        if (userProfile != null) {
            SellerProfileCard(
                sellerId = userProfile.sellerId?.toInt(),
                sellerName = userProfile.sellerName ?: "Vendedor",
                branchName = userProfile.branchName ?: "Sucursal Principal",
                branchCode = userProfile.branchCode,
                affiliationCode = userProfile.affiliationCode,
                connectionState = connectionState
            )
        } else {
            // Estado de carga para el perfil
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
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Cargando perfil...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Obteniendo información del vendedor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
