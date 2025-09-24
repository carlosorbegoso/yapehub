package org.sysarp.project.ui.screens.seller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.SellerFinancialAnalysisData
import org.sysarp.project.ui.components.admin.ChartItem
import org.sysarp.project.ui.components.admin.ResponsiveChartRow
import org.sysarp.project.ui.components.charts.AchievementsChart
import org.sysarp.project.ui.components.charts.ComparisonsChart
import org.sysarp.project.ui.common.components.charts.DailySalesBarChart
import org.sysarp.project.ui.components.charts.GoalsProgressChart
import org.sysarp.project.ui.components.charts.HourlySalesChart
import org.sysarp.project.ui.common.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.components.charts.PredictionsChart
import org.sysarp.project.ui.components.charts.SalesDistributionChart
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.components.financial.SellerFinancialAnalysisCard

/**
 * Componente para las secciones de analytics del vendedor
 */
@Composable
fun SellerAnalyticsSections(
    analyticsData: AnalyticsData,
    sellerFinancialData: SellerFinancialAnalysisData?,
    showBasicCharts: Boolean,
    showAdvancedCharts: Boolean,
    showPredictiveCharts: Boolean,
    showAdditionalMetrics: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Métricas principales destacadas
        SellerPrimaryMetricsSection(data = analyticsData.overview)
        
        // Sección de gráficos básicos - Layout horizontal para pantallas grandes
        if (showBasicCharts) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(600, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 Gráficos Principales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gráficos básicos en layout responsivo
                    ResponsiveChartRow(
                        charts = listOf(
                            ChartItem("Ventas Diarias", { DailySalesBarChart(dailySales = analyticsData.dailySales, showCard = false) }),
                            ChartItem("Métricas de Rendimiento", { PerformanceMetricsPieChart(performanceMetrics = analyticsData.performanceMetrics, showCard = false) }),
                            ChartItem("Tendencias", { SalesTrendLineChart(dailySales = analyticsData.dailySales, showCard = false) })
                        )
                    )
                }
            }
        }

        // Sección de gráficos avanzados - Layout horizontal optimizado
        if (showAdvancedCharts) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📈 Análisis Avanzado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gráficos avanzados en layout responsivo
                    ResponsiveChartRow(
                        charts = listOf(
                            ChartItem("Ventas por Hora", { HourlySalesChart(hourlySales = analyticsData.hourlySales, showCard = false) }),
                            ChartItem("Progreso de Metas", { GoalsProgressChart(sellerGoals = analyticsData.sellerGoals, showCard = false) }),
                            ChartItem("Logros", { AchievementsChart(sellerAchievements = analyticsData.sellerAchievements, showCard = false) })
                        )
                    )
                }
            }
        }

        // Sección de análisis predictivo - Layout horizontal para pantallas grandes
        if (showPredictiveCharts) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(1000, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔮 Análisis Predictivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gráficos de análisis avanzado en layout responsivo
                    ResponsiveChartRow(
                        charts = listOf(
                            ChartItem("Predicciones", { PredictionsChart(sellerForecasting = analyticsData.sellerForecasting, showCard = false) }),
                            ChartItem("Distribución", { SalesDistributionChart(salesDistribution = analyticsData.sellerAnalytics?.salesDistribution) }),
                            ChartItem("Comparaciones", { ComparisonsChart(sellerComparisons = analyticsData.sellerComparisons) })
                        )
                    )
                }
            }
        }

        // Información adicional de métricas
        if (showAdditionalMetrics) {
            SellerAdditionalMetricsCard(data = analyticsData.overview)
        }
        
        // Sección de Análisis Financiero - Nueva funcionalidad
        sellerFinancialData?.let { financial ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(2200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(2200))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "💰 Análisis Financiero",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    SellerFinancialAnalysisCard(data = financial)
                }
            }
        }
    }
}
