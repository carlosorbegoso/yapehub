package org.sysarp.project.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData
import org.sysarp.project.ui.common.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.common.components.charts.daily.sales.DailySalesChart
import org.sysarp.project.ui.common.components.charts.hourly.sales.HourlySalesChart
import org.sysarp.project.ui.components.admin.ChartItem
import org.sysarp.project.ui.components.admin.ResponsiveChartRow
import org.sysarp.project.ui.components.charts.AchievementsChart
import org.sysarp.project.ui.components.charts.ComparisonsChart
import org.sysarp.project.ui.components.charts.GoalsProgressChart
import org.sysarp.project.ui.common.components.charts.predictions.PredictionsChart
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
    // Detección de tamaño de pantalla para responsive design
    // Usar valores fijos para multiplataforma
    val screenWidth = 400.dp // Valor por defecto
    val isLargeScreen = screenWidth >= 840.dp
    
    // Estados para animaciones escalonadas
    var showMetrics by remember { mutableStateOf(false) }
    var showBasic by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }
    var showPredictive by remember { mutableStateOf(false) }
    var showAdditional by remember { mutableStateOf(false) }
    
    // Animaciones escalonadas
    LaunchedEffect(Unit) {
        showMetrics = true
        kotlinx.coroutines.delay(100)
        showBasic = true
        kotlinx.coroutines.delay(200)
        showAdvanced = true
        kotlinx.coroutines.delay(200)
        showPredictive = true
        kotlinx.coroutines.delay(200)
        showAdditional = true
    }
    
    if (isLargeScreen) {
        // Layout horizontal para pantallas grandes (tablets y desktop)
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Columna izquierda - Métricas y gráficos básicos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Métricas principales destacadas
                AnimatedVisibility(
                    visible = showMetrics,
                    enter = scaleIn(
                        animationSpec = tween(800, easing = EaseOutCubic),
                        initialScale = 0.8f
                    ) + fadeIn(animationSpec = tween(800))
                ) {
                    AdminPrimaryMetricsSection(data = analyticsData.overview)
                }
                
                // Sección de gráficos básicos
                if (showBasicCharts) {
                    AnimatedVisibility(
                        visible = showBasic,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                            animationSpec = tween(800, easing = EaseOutCubic),
                            initialScale = 0.9f
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📊 Gráficos Principales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            // Gráficos básicos en layout responsivo
                            ResponsiveChartRow(
                                charts = listOf(
                                    ChartItem("Ventas Diarias", { DailySalesChart(dailySales = analyticsData.dailySales, showCard = false) }),
                                    ChartItem("Métricas de Rendimiento", { PerformanceMetricsPieChart(performanceMetrics = analyticsData.performanceMetrics, showCard = false) }),
                                    ChartItem("Tendencias", { SalesTrendLineChart(dailySales = analyticsData.dailySales, showCard = false) })
                                )
                            )
                        }
                    }
                }
            }
            
            // Columna derecha - Gráficos avanzados y predictivos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sección de gráficos avanzados
                if (showAdvancedCharts) {
                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                            animationSpec = tween(800, easing = EaseOutCubic),
                            initialScale = 0.9f
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📈 Análisis Avanzado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
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
                
                // Sección de análisis predictivo
                if (showPredictiveCharts) {
                    AnimatedVisibility(
                        visible = showPredictive,
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(800, easing = EaseOutCubic)
                        ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                            animationSpec = tween(800, easing = EaseOutCubic),
                            initialScale = 0.9f
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🔮 Análisis Predictivo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            
                            // Gráficos predictivos en layout responsivo
                            ResponsiveChartRow(
                                charts = listOf(
                                    ChartItem("Predicciones", { PredictionsChart(sellerForecasting = analyticsData.sellerForecasting, showCard = false) }),
                                    ChartItem("Comparaciones", { ComparisonsChart(sellerComparisons = analyticsData.sellerComparisons, showCard = false) }),
                                    ChartItem("Distribución", { SalesDistributionChart(salesDistribution = analyticsData.sellerAnalytics?.salesDistribution, showCard = false) })
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Información adicional de métricas (ancho completo)
        if (showAdditionalMetrics) {
            AnimatedVisibility(
                visible = showAdditional,
                enter = scaleIn(
                    animationSpec = tween(800, easing = EaseOutCubic),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(800))
            ) {
                AdminAdditionalMetricsCard(data = analyticsData.overview)
            }
        }
        
        // Sección de Análisis Financiero (ancho completo)
        financialData?.let { financial ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                FinancialAnalysisCard(data = financial)
            }
        }
        
        // Sección de Transparencia de Pagos (ancho completo)
        transparencyData?.let { transparency ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                PaymentTransparencyCard(data = transparency)
            }
        }
    } else {
        // Layout vertical para móviles y tablets pequeñas
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Métricas principales destacadas
        AnimatedVisibility(
            visible = showMetrics,
            enter = scaleIn(
                animationSpec = tween(800, easing = EaseOutCubic),
                initialScale = 0.8f
            ) + fadeIn(animationSpec = tween(800))
        ) {
            AdminPrimaryMetricsSection(data = analyticsData.overview)
        }
        
        // Sección de gráficos básicos - Layout horizontal para pantallas grandes
        if (showBasicCharts) {
            AnimatedVisibility(
                visible = showBasic,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                    animationSpec = tween(800, easing = EaseOutCubic),
                    initialScale = 0.9f
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 Gráficos Principales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gráficos básicos en layout responsivo
                    ResponsiveChartRow(
                        charts = listOf(
                            ChartItem("Ventas Diarias", { DailySalesChart(dailySales = analyticsData.dailySales, showCard = false) }),
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
                visible = showAdvanced,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                    animationSpec = tween(800, easing = EaseOutCubic),
                    initialScale = 0.9f
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📈 Análisis Avanzado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                visible = showPredictive,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(800, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(800)) + scaleIn(
                    animationSpec = tween(800, easing = EaseOutCubic),
                    initialScale = 0.9f
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔮 Análisis Predictivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gráficos de análisis avanzado en layout responsivo
                    ResponsiveChartRow(
                        charts = listOf(
                            ChartItem("Predicciones", { PredictionsChart(sellerForecasting = analyticsData.sellerForecasting, showCard = false) }),
                            ChartItem("Distribución", { SalesDistributionChart(salesDistribution = analyticsData.sellerAnalytics?.salesDistribution, showCard = false) }),
                            ChartItem("Comparaciones", { ComparisonsChart(sellerComparisons = analyticsData.sellerComparisons, showCard = false) })
                        )
                    )
                }
            }
        }

        // Información adicional de métricas
        if (showAdditionalMetrics) {
            AnimatedVisibility(
                visible = showAdditional,
                enter = scaleIn(
                    animationSpec = tween(800, easing = EaseOutCubic),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(800))
            ) {
                AdminAdditionalMetricsCard(data = analyticsData.overview)
            }
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    PaymentTransparencyCard(data = transparency)
                }
            }
        }
    }
    }
}
