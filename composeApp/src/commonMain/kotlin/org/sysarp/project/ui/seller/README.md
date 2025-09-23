# 📁 Estructura Organizada de Seller

Esta carpeta contiene todos los archivos relacionados con la funcionalidad de **Vendedores (Sellers)** del proyecto YapeChamo, organizados de manera lógica y mantenible.

## 🏗️ Estructura de Carpetas

```
ui/seller/
├── components/           # Componentes reutilizables
│   ├── cards/           # Tarjetas y componentes de tarjeta
│   ├── dashboard/        # Componentes específicos del dashboard
│   ├── dialogs/          # Diálogos y modales
│   └── forms/            # Formularios y validaciones
├── screens/              # Pantallas principales
│   ├── analytics/        # Pantallas de análisis y estadísticas
│   ├── auth/             # Pantallas de autenticación
│   ├── dashboard/        # Pantalla principal del dashboard
│   ├── notifications/    # Pantallas de notificaciones
│   └── payments/         # Pantallas de pagos
├── services/             # Servicios y APIs
└── data/                 # Modelos de datos
```

## 📋 Descripción de Componentes

### 🎨 Components

#### **Cards** (`components/cards/`)
- `SellerProfileCard.kt` - Tarjeta de perfil del vendedor
- `SellerFinancialAnalysisCard.kt` - Tarjeta de análisis financiero
- `SellerFinancialFilterDialog.kt` - Diálogo de filtros financieros

#### **Dashboard** (`components/dashboard/`)
- `SellerDashboardActions.kt` - Acciones del dashboard
- `SellerDashboardContent.kt` - Contenido principal del dashboard
- `SellerDashboardTopBar.kt` - Barra superior del dashboard
- `cards/` - Tarjetas específicas del dashboard
- `sections/` - Secciones del dashboard
- `utils/` - Utilidades del dashboard

#### **Dialogs** (`components/dialogs/`)
- `BranchSellersDialog.kt` - Diálogo de vendedores por sucursal
- `SellerSectionFiltersDialog.kt` - Diálogo de filtros por sección

#### **Forms** (`components/forms/`)
- `seller_unified/` - Formulario unificado de vendedor
  - `actions/` - Acciones del formulario
  - `animations/` - Animaciones del formulario
  - `fields/` - Campos del formulario
  - `validation/` - Validaciones del formulario

### 📱 Screens

#### **Analytics** (`screens/analytics/`)
- `SellerAnalyticsScreen.kt` - Pantalla principal de analytics
- `SellerAnalyticsComponents.kt` - Componentes de analytics
- `SellerAnalyticsControls.kt` - Controles de analytics
- `SellerAnalyticsDataLoader.kt` - Cargador de datos
- `SellerAnalyticsDialogs.kt` - Diálogos de analytics
- `SellerAnalyticsSections.kt` - Secciones de analytics
- `SellerAnalyticsState.kt` - Estado de analytics
- `SellerAnalyticsStates.kt` - Estados de analytics

#### **Auth** (`screens/auth/`)
- `SellerUnifiedScreen.kt` - Pantalla unificada de acceso
- `SellerQRLoginScreen.kt` - Pantalla de login con QR

#### **Dashboard** (`screens/dashboard/`)
- `SellerDashboardScreen.kt` - Pantalla principal del dashboard

#### **Notifications** (`screens/notifications/`)
- `SellerNotificationsScreen.kt` - Pantalla de notificaciones

#### **Payments** (`screens/payments/`)
- `SellerPaymentsScreen.kt` - Pantalla de pagos
- `SellerSpecificPaymentsScreen.kt` - Pantalla de pagos específicos

### 🔧 Services

#### **APIs** (`services/`)
- `SellerService.kt` - Servicio principal de vendedores
- `SellerAuthApiClient.kt` - Cliente de API de autenticación
- `AdminSellerApiClient.kt` - Cliente de API para administradores
- `seller/` - Servicios específicos de vendedores
  - `SellerManagementApiClient.kt` - API de gestión
  - `SellerRegistrationApiClient.kt` - API de registro

### 📊 Data

#### **Models** (`data/`)
- `SellerModels.kt` - Modelos de datos de vendedores

## 🎯 Beneficios de esta Organización

1. **📁 Separación Clara**: Cada tipo de componente tiene su lugar específico
2. **🔍 Fácil Navegación**: Estructura intuitiva y lógica
3. **♻️ Reutilización**: Componentes organizados por funcionalidad
4. **🧪 Testing**: Más fácil escribir y organizar tests
5. **👥 Colaboración**: Múltiples desarrolladores pueden trabajar sin conflictos
6. **📈 Escalabilidad**: Fácil agregar nuevas funcionalidades
7. **🔧 Mantenimiento**: Cambios específicos en carpetas específicas

## 📝 Convenciones de Nomenclatura

- **Archivos**: `Seller[Funcionalidad][Tipo].kt`
- **Carpetas**: `snake_case` para funcionalidades específicas
- **Componentes**: `PascalCase` para nombres de componentes
- **Servicios**: `[Funcionalidad]Service.kt` o `[Funcionalidad]ApiClient.kt`

## 🚀 Uso

Para usar cualquier componente de esta estructura, simplemente importa desde la ruta correspondiente:

```kotlin
import org.sysarp.project.ui.seller.components.cards.SellerProfileCard
import org.sysarp.project.ui.seller.screens.dashboard.SellerDashboardScreen
import org.sysarp.project.ui.seller.services.SellerService
```

---

*Esta estructura fue creada para mejorar la organización y mantenibilidad del código relacionado con vendedores en YapeChamo.*
