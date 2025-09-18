# YapeHub - Capturador de Notificaciones de Yape

YapeHub es una aplicación móvil desarrollada en Kotlin Multiplatform que te ayuda a capturar y organizar las notificaciones de Yape para generar reportes detallados de tus negocios. Perfecto para emprendedores que manejan múltiples negocios y necesitan un control preciso de sus ingresos.

## 🚀 Características

- **Captura Automática**: Captura automáticamente las notificaciones de Yape
- **Organización por Negocios**: Asigna transacciones a diferentes negocios
- **Reportes Detallados**: Genera reportes por negocio y por fecha
- **Exportación**: Exporta reportes en formato CSV y PDF
- **Interfaz Intuitiva**: Diseño moderno y fácil de usar
- **Multiplataforma**: Funciona en Android e iOS

## 📱 Funcionalidades Principales

### Pantalla Principal
- Estado de captura de notificaciones
- Estadísticas en tiempo real (total recibido, enviado, sin procesar)
- Lista de transacciones recientes
- Acciones rápidas para procesar transacciones

### Reportes
- **Por Negocio**: Ve el total de ingresos por cada negocio
- **Diario**: Reportes organizados por fecha
- **Exportación**: Descarga reportes en CSV o PDF

### Configuración
- Control de captura de notificaciones
- Información sobre permisos necesarios
- Información de la aplicación

## 🛠️ Instalación y Configuración

### Requisitos
- Android 7.0 (API 24) o superior
- iOS 13.0 o superior
- Permisos de acceso a notificaciones

### Configuración en Android

1. **Instalar la aplicación** desde el archivo APK
2. **Otorgar permisos de notificaciones**:
   - Ve a Configuración > Aplicaciones > YapeHub > Notificaciones
   - Activa "Permitir acceso a notificaciones"
3. **Habilitar el servicio de notificaciones**:
   - Ve a Configuración > Accesibilidad > Servicios instalados
   - Busca "YapeHub" y actívalo
4. **Abrir la aplicación** y presionar "Iniciar Captura"

### Configuración en iOS

1. **Instalar la aplicación** desde la App Store
2. **Otorgar permisos** cuando la aplicación los solicite
3. **Configurar notificaciones** en Configuración > Notificaciones > YapeChamo

## 📊 Cómo Usar la Aplicación

### 1. Configuración Inicial
1. Abre la aplicación
2. Ve a Configuración y otorga los permisos necesarios
3. Regresa a la pantalla principal y presiona "Iniciar Captura"

### 2. Procesar Transacciones
1. Las notificaciones de Yape se capturarán automáticamente
2. Ve a la pantalla principal para ver las transacciones
3. Para cada transacción:
   - **Procesar**: Marca como procesada
   - **Asignar Negocio**: Asigna a un negocio específico
   - **Eliminar**: Elimina transacciones incorrectas

### 3. Generar Reportes
1. Ve a la pestaña "Reportes"
2. Selecciona el tipo de reporte:
   - **Por Negocio**: Ve ingresos por negocio
   - **Diario**: Ve ingresos por fecha
   - **Exportar**: Descarga reportes

### 4. Exportar Datos
1. En la pestaña "Exportar" de Reportes
2. Selecciona el formato (CSV o PDF)
3. Los archivos se guardarán en tu dispositivo

## 🔧 Desarrollo

### Tecnologías Utilizadas
- **Kotlin Multiplatform**: Código compartido entre Android e iOS
- **Jetpack Compose**: Interfaz de usuario moderna
- **Sin base de datos local**: Solo datos en memoria
- **Kotlinx Serialization**: Serialización de datos
- **Kotlinx Coroutines**: Programación asíncrona

### Estructura del Proyecto
```
composeApp/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── data/           # Modelos de datos
│   │   │   ├── data/           # Modelos de datos
│   │   │   ├── repository/     # Repositorio de datos
│   │   │   ├── service/        # Servicios de captura
│   │   │   ├── ui/            # Interfaz de usuario
│   │   │   └── viewmodel/     # ViewModels
│   │   └── data/              # Modelos de datos
│   ├── androidMain/           # Implementación Android
│   └── iosMain/              # Implementación iOS
```

### Compilar el Proyecto

#### Android
```bash
./gradlew :composeApp:assembleDebug
```

#### iOS
```bash
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

## 📋 Permisos Necesarios

### Android
- `BIND_NOTIFICATION_LISTENER_SERVICE`: Para capturar notificaciones
- `SYSTEM_ALERT_WINDOW`: Para mostrar ventanas del sistema
- `WRITE_EXTERNAL_STORAGE`: Para exportar archivos
- `READ_EXTERNAL_STORAGE`: Para leer archivos exportados

### iOS
- Acceso a notificaciones (limitado por las políticas de Apple)

## 🐛 Solución de Problemas

### La aplicación no captura notificaciones
1. Verifica que los permisos estén otorgados
2. Asegúrate de que el servicio de notificaciones esté habilitado
3. Reinicia la aplicación
4. Verifica que Yape esté instalado y funcionando

### Las transacciones no se procesan correctamente
1. Verifica que las notificaciones de Yape estén en español
2. Revisa el formato de las notificaciones en la configuración
3. Reporta problemas específicos para mejorar el parsing

### Problemas de exportación
1. Verifica que tengas espacio en el dispositivo
2. Asegúrate de que los permisos de almacenamiento estén otorgados
3. Intenta exportar en un formato diferente

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

Si tienes problemas o preguntas:
1. Revisa la sección de solución de problemas
2. Busca en los issues existentes
3. Crea un nuevo issue con detalles del problema

## 🔮 Roadmap

- [ ] Sincronización en la nube
- [ ] Múltiples usuarios/empleados
- [ ] Integración con sistemas de contabilidad
- [ ] Notificaciones push para reportes
- [ ] Análisis de tendencias
- [ ] Backup automático

---

**YapeChamo** - Simplificando el control de tus negocios con Yape 🚀# yapehub
