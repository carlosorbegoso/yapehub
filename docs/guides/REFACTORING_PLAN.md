# 📋 Plan de Refactorización - Archivos Grandes

## 🎯 Objetivo
Dividir archivos grandes (>600 líneas) en componentes más pequeños y manejables (<300 líneas cada uno)

## 📊 Archivos a Refactorizar (Top 10)

### 1. ✅ **PredictionsChart.kt** - 835 líneas
**Dividir en:**
- `predictions/PredictionsChart.kt` (componente principal) - ~100 líneas
- `predictions/PredictionsLineChart.kt` (gráfico de líneas) - ~250 líneas
- `predictions/TrendAnalysisSection.kt` (análisis de tendencias) - ~150 líneas
- `predictions/RecommendationsSection.kt` (recomendaciones) - ~150 líneas
- `predictions/PredictionsUtils.kt` (funciones de utilidad) - ~150 líneas
- `predictions/PredictionsDrawing.kt` (funciones de dibujo) - ~200 líneas

**Beneficios:**
- ✅ Código más legible y mantenible
- ✅ Mejor testing (componentes independientes)
- ✅ Reutilización de componentes
- ✅ Más fácil de depurar

---

### 2. **DailySalesChart.kt** - 725 líneas
**Dividir en:**
- `daily-sales/DailySalesChart.kt` (componente principal)
- `daily-sales/DailySalesLineChart.kt` (gráfico de líneas)
- `daily-sales/DailySalesBarChart.kt` (gráfico de barras)
- `daily-sales/DailySalesLegend.kt` (leyenda)
- `daily-sales/DailySalesUtils.kt` (utilidades)

---

### 3. **BranchManagementComponentsUI.kt** - 692 líneas
**Dividir en:**
- `branch-management/BranchList.kt`
- `branch-management/BranchCard.kt`
- `branch-management/BranchDialog.kt`
- `branch-management/BranchFilters.kt`

---

### 4. **AdminPaymentsComponents.kt** - 651 líneas
**Dividir en:**
- `admin-payments/PaymentsList.kt`
- `admin-payments/PaymentCard.kt`
- `admin-payments/PaymentFilters.kt`
- `admin-payments/PaymentDialogs.kt`

---

### 5. **GenerateAffiliationCodeDialog.kt** - 628 líneas
**Dividir en:**
- `affiliation/AffiliationCodeDialog.kt`
- `affiliation/AffiliationForm.kt`
- `affiliation/AffiliationValidation.kt`

---

### 6. **UserManagementScreen.kt** - 625 líneas
**Dividir en:**
- `user-management/UserManagementScreen.kt` (componente principal)
- `user-management/UserList.kt`
- `user-management/UserCard.kt`
- `user-management/UserDialog.kt`

---

### 7. **SellerPaymentsComponents.kt** - 624 líneas
**Dividir en:**
- `seller-payments/SellerPaymentsList.kt`
- `seller-payments/SellerPaymentCard.kt`
- `seller-payments/SellerPaymentFilters.kt`

---

### 8. **BillingDashboardCards.kt** - 612 líneas
**Dividir en:**
- `billing/BillingOverviewCard.kt`
- `billing/BillingDetailsCard.kt`
- `billing/BillingHistoryCard.kt`

---

### 9. **LoginScreen.kt** - 599 líneas
**Dividir en:**
- `login/LoginScreen.kt` (componente principal)
- `login/LoginForm.kt`
- `login/LoginValidation.kt`
- `login/LoginHeader.kt`

---

### 10. **ServerQRDisplayScreen.kt** - 590 líneas
**Dividir en:**
- `qr/QRDisplayScreen.kt`
- `qr/QRGenerator.kt`
- `qr/QRScanner.kt`

---

## 🚀 Estrategia de Refactorización

### Fase 1: Charts (Gráficos)
1. ✅ PredictionsChart.kt
2. DailySalesChart.kt
3. PerformanceMetricsPieChart.kt

### Fase 2: Management Screens (Pantallas de gestión)
4. BranchManagementComponentsUI.kt
5. UserManagementScreen.kt

### Fase 3: Payment Components (Componentes de pago)
6. AdminPaymentsComponents.kt
7. SellerPaymentsComponents.kt

### Fase 4: Other Screens (Otras pantallas)
8. LoginScreen.kt
9. BillingDashboardCards.kt
10. ServerQRDisplayScreen.kt

---

## 📈 Métricas de Éxito

### Antes de Refactorización
- **Archivos >600 líneas:** 10 archivos
- **Total de líneas:** ~6,500 líneas
- **Promedio por archivo:** 650 líneas
- **Tiempo de compilación:** ~40 segundos

### Después de Refactorización (Objetivo)
- **Archivos >600 líneas:** 0 archivos
- **Total de líneas:** ~6,500 líneas (sin cambios)
- **Promedio por archivo:** <250 líneas
- **Tiempo de compilación:** ~25 segundos
- **Mantenibilidad:** +70%
- **Testabilidad:** +80%

---

## ✅ Checklist de Refactorización

Por cada archivo:
- [ ] Identificar componentes independientes
- [ ] Crear estructura de directorios
- [ ] Extraer componentes a archivos separados
- [ ] Actualizar imports
- [ ] Verificar compilación
- [ ] Ejecutar tests (si existen)
- [ ] Documentar cambios

---

## 🎯 Estado Actual

- ✅ **Iniciado:** PredictionsChart.kt
- ⏳ **En progreso:** Creando estructura de componentes
- ⏹️ **Pendiente:** Resto de archivos

---

## 📝 Notas

- Mantener la funcionalidad existente intacta
- No cambiar la API pública de los componentes
- Asegurar que todos los imports se actualicen correctamente
- Documentar cualquier cambio significativo

