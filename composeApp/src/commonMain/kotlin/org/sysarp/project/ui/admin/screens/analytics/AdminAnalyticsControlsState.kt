package org.sysarp.project.ui.admin.screens.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

/**
 * Estado y lógica de negocio para AdminAnalyticsControls
 */
class AdminAnalyticsControlsState {
    // Estados de UI
    var showPeriodMenu by mutableStateOf(false)
        private set
    
    var selectedPeriod by mutableStateOf("📅 30 días")
        private set
    
    // Estados de filtros
    var showFiltersDialog by mutableStateOf(false)
        private set
    
    var showFinancialFiltersDialog by mutableStateOf(false)
        private set
    
    var showTransparencyFiltersDialog by mutableStateOf(false)
        private set
    
    /**
     * Establece el período seleccionado
     */
    fun updateSelectedPeriod(period: String) {
        selectedPeriod = period
    }
    
    /**
     * Establece si el menú de período está visible
     */
    fun updateShowPeriodMenu(show: Boolean) {
        showPeriodMenu = show
    }
    
    /**
     * Establece si el diálogo de filtros está visible
     */
    fun updateShowFiltersDialog(show: Boolean) {
        showFiltersDialog = show
    }
    
    /**
     * Establece si el diálogo de filtros financieros está visible
     */
    fun updateShowFinancialFiltersDialog(show: Boolean) {
        showFinancialFiltersDialog = show
    }
    
    /**
     * Establece si el diálogo de filtros de transparencia está visible
     */
    fun updateShowTransparencyFiltersDialog(show: Boolean) {
        showTransparencyFiltersDialog = show
    }
    
    /**
     * Obtiene las fechas correspondientes al período seleccionado
     */
    fun getPeriodDates(): Pair<String, String> {
        val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        
        return when (selectedPeriod) {
            "📅 Hoy" -> {
                Pair(endDate, endDate)
            }
            "📅 7 días" -> {
                val startDate = Clock.System.now().minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                Pair(startDate, endDate)
            }
            "📅 30 días" -> {
                val startDate = Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                Pair(startDate, endDate)
            }
            "📅 90 días" -> {
                val startDate = Clock.System.now().minus(90.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                Pair(startDate, endDate)
            }
            else -> {
                val startDate = Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                Pair(startDate, endDate)
            }
        }
    }
    
    /**
     * Verifica si el menú de período está visible
     */
    fun isPeriodMenuVisible(): Boolean {
        return showPeriodMenu
    }
    
    /**
     * Verifica si algún diálogo de filtros está visible
     */
    fun isAnyFilterDialogVisible(): Boolean {
        return showFiltersDialog || showFinancialFiltersDialog || showTransparencyFiltersDialog
    }
    
    /**
     * Cierra todos los diálogos
     */
    fun closeAllDialogs() {
        showPeriodMenu = false
        showFiltersDialog = false
        showFinancialFiltersDialog = false
        showTransparencyFiltersDialog = false
    }
    
    /**
     * Cierra el menú de período
     */
    fun closePeriodMenu() {
        showPeriodMenu = false
    }
    
    /**
     * Abre el menú de período
     */
    fun openPeriodMenu() {
        showPeriodMenu = true
    }
    
    /**
     * Abre el diálogo de filtros
     */
    fun openFiltersDialog() {
        showFiltersDialog = true
    }
    
    /**
     * Abre el diálogo de filtros financieros
     */
    fun openFinancialFiltersDialog() {
        showFinancialFiltersDialog = true
    }
    
    /**
     * Abre el diálogo de filtros de transparencia
     */
    fun openTransparencyFiltersDialog() {
        showTransparencyFiltersDialog = true
    }
    
    /**
     * Obtiene el período actual formateado
     */
    fun getCurrentPeriodFormatted(): String {
        return selectedPeriod
    }
    
    /**
     * Verifica si el período seleccionado es válido
     */
    fun isValidPeriod(): Boolean {
        return selectedPeriod.isNotBlank() && selectedPeriod.startsWith("📅")
    }
    
    /**
     * Obtiene el número de días del período seleccionado
     */
    fun getPeriodDays(): Int {
        return when (selectedPeriod) {
            "📅 Hoy" -> 1
            "📅 7 días" -> 7
            "📅 30 días" -> 30
            "📅 90 días" -> 90
            else -> 30
        }
    }
}
