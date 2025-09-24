package org.sysarp.project.ui.screens.admin

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
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData
import org.sysarp.project.ui.components.admin.ChartItem
import org.sysarp.project.ui.components.admin.ResponsiveChartRow
import org.sysarp.project.ui.components.charts.AchievementsChart
import org.sysarp.project.ui.components.charts.ComparisonsChart
import org.sysarp.project.ui.common.components.charts.DailySalesBarChart
import org.sysarp.project.ui.components.charts.GoalsProgressChart
import org.sysarp.project.ui.components.charts.HourlySalesChart
import org.sysarp.project.ui.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.components.charts.PredictionsChart
import org.sysarp.project.ui.components.charts.SalesDistributionChart
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.components.financial.FinancialAnalysisCard
import org.sysarp.project.ui.components.financial.PaymentTransparencyCard

/**
 * Componente para las secciones de analytics del administrador
 */
@Composable
fun AdminAnalyticsSections(
    analyticsData: AnalyticsData,
    financialData: FinancialAnalysisData?,
    transparencyData: PaymentTransparencyData?,
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
        AdminPrimaryMetricsSection(data = analyticsData.overview)
        
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
                            ChartItem("Métricas de Rendimiento", { PerformanceMetricsPieChart(performanceMetrics = analyticsData.performanceMetrics) }),
                            ChartItem("Tendencias", { SalesTrendLineChart(dailySales = analyticsData.dailySales) })
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
                            ChartItem("Ventas por Hora", { HourlySalesChart(hourlySales = analyticsData.hourlySales) }),
                            ChartItem("Progreso de Metas", { GoalsProgressChart(sellerGoals = analyticsData.sellerGoals) }),
                            ChartItem("Logros", { AchievementsChart(sellerAchievements = analyticsData.sellerAchievements) })
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
                            ChartItem("Predicciones", { PredictionsChart(sellerForecasting = analyticsData.sellerForecasting) }),
                            ChartItem("Distribución", { SalesDistributionChart(salesDistribution = analyticsData.sellerAnalytics?.salesDistribution) }),
                            ChartItem("Comparaciones", { ComparisonsChart(sellerComparisons = analyticsData.sellerComparisons) })
                        )
                    )
                }
            }
        }

        // Información adicional de métricas
        if (showAdditionalMetrics) {
            AdminAdditionalMetricsCard(data = analyticsData.overview)
        }
        
        // Sección de Análisis Financiero
        financialData?.let { financial ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(1200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(1200))
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
                    
                    FinancialAnalysisCard(data = financial)
                }
            }
        }
        
        // Sección de Transparencia de Pagos
        transparencyData?.let { transparency ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(1400, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(1400))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔍 Transparencia de Pagos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    PaymentTransparencyCard(data = transparency)
                }
            }
        }
    }
}
