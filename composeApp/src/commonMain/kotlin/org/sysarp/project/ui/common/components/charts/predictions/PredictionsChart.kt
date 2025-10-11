package org.sysarp.project.ui.common.components.charts.predictions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerForecastingData

/**
 * Gráfico principal de predicciones
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun PredictionsChart(
    sellerForecasting: SellerForecastingData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerForecasting == null) {
            EmptyPredictionsState(showPadding = showCard)
        } else {
            PredictionsContent(
                sellerForecasting = sellerForecasting,
                showPadding = showCard
            )
        }
    }

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
        }
    }
}

@Composable
private fun EmptyPredictionsState(showPadding: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (showPadding) 16.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No hay datos de predicciones disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PredictionsContent(
    sellerForecasting: SellerForecastingData,
    showPadding: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (showPadding) 16.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gráfico de líneas mejorado con predicciones
        PredictionsLineChart(
            predictedSales = sellerForecasting.predictedSales,
            trendAnalysis = sellerForecasting.trendAnalysis,
            showCard = false
        )

        // Análisis de tendencias
        TrendAnalysisSection(trendAnalysis = sellerForecasting.trendAnalysis)

        // Recomendaciones
        if (sellerForecasting.recommendations.isNotEmpty()) {
            RecommendationsSection(recommendations = sellerForecasting.recommendations)
        }
    }
}

