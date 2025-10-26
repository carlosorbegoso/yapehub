package org.sysarp.project.ui.common.components.charts.daily.sales

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.components.DailySalesChart

/**
 * Gráfico de barras mejorado para ventas diarias
 * Refactorizado para ser más modular y mantenible
 * 
 * Características principales:
 * - Barras dinámicas que se adaptan al número de días
 * - Animaciones suaves y escalonadas
 * - Efectos de hover con tooltips informativos
 * - Análisis inteligente de tendencias y métricas
 * - Diálogo de detalles al hacer clic en una barra
 * - Diseño responsivo y adaptable
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun DailySalesChart(
    dailySales: List<DailySalesData>,
    title: String = "Ventas Diarias",
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    DailySalesChart(
        dailySales = dailySales,
        title = title,
        showCard = showCard,
        modifier = modifier
    )
}
