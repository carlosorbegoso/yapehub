# 🏪 Guía de Publicación en Google Play Store - YapeHub

Esta guía te llevará paso a paso por el proceso de publicar YapeHub en Google Play Store.

## 📋 Requisitos Previos

### Archivos Necesarios:
- ✅ `composeApp-release.aab` (Android App Bundle)
- ✅ Keystore de firma (`yapehub-release.keystore`)
- ✅ Íconos de la aplicación en diferentes tamaños
- ✅ Screenshots de la aplicación
- ✅ Descripción de la aplicación

### Cuentas Requeridas:
- **Google Play Console** - [Registrarse](https://play.google.com/console)
- **Cuenta de desarrollador** ($25 USD una sola vez)

## 🚀 Paso 1: Preparar el AAB

### Generar Android App Bundle:
```bash
# Limpiar proyecto
./scripts/clean-project.sh

# Generar AAB firmado
./scripts/build-bundle.sh

# Verificar archivo
./scripts/verify-apk.sh
```

### Verificar el AAB:
```bash
# Ubicación del archivo
ls -lh composeApp/build/outputs/bundle/release/composeApp-release.aab

# Debería mostrar ~27MB
```

## 🎨 Paso 2: Preparar Assets

### Íconos Requeridos:
- **Ícono de aplicación**: 512x512 px (PNG)
- **Ícono adaptativo**: 512x512 px (PNG, con foreground y background)
- **Ícono de funcionalidad**: 512x512 px (PNG)

### Screenshots Requeridos:
- **Teléfono**: Mínimo 2, máximo 8 screenshots
- **Tablet 7"**: Opcional pero recomendado
- **Tablet 10"**: Opcional pero recomendado

### Tamaños de Screenshots:
- **Teléfono**: 16:9 o 9:16 ratio
- **Resolución mínima**: 320px
- **Resolución máxima**: 3840px

## 📝 Paso 3: Crear Aplicación en Play Console

### 1. **Acceder a Play Console**
- Ve a [Google Play Console](https://play.google.com/console)
- Inicia sesión con tu cuenta de desarrollador

### 2. **Crear Nueva Aplicación**
- Clic en "Crear aplicación"
- Nombre: **YapeHub**
- Idioma predeterminado: **Español (España)**
- Tipo: **Aplicación**
- Categoría: **Finanzas**

### 3. **Configuración Básica**
```
Nombre de la aplicación: YapeHub
Descripción corta: Captura y organiza notificaciones de Yape
Descripción completa: [Ver sección de descripción]
```

## 📱 Paso 4: Configurar Ficha de Play Store

### Descripción Completa:
```
YapeHub - Capturador de Notificaciones de Yape

YapeHub es una aplicación móvil que te ayuda a capturar y organizar las notificaciones de Yape para generar reportes detallados de tus negocios. Perfecto para emprendedores que manejan múltiples negocios y necesitan un control preciso de sus ingresos.

🚀 CARACTERÍSTICAS PRINCIPALES:
• Captura automática de notificaciones de Yape
• Organización por negocios diferentes
• Reportes detallados por negocio y fecha
• Exportación en formato CSV y PDF
• Interfaz intuitiva y fácil de usar
• Funciona sin conexión a internet

📊 FUNCIONALIDADES:
• Estado de captura en tiempo real
• Estadísticas de ingresos y transacciones
• Lista de transacciones recientes
• Procesamiento manual de transacciones
• Asignación a diferentes negocios
• Generación de reportes automáticos

💼 IDEAL PARA:
• Emprendedores con múltiples negocios
• Comerciantes que usan Yape
• Personas que necesitan control de ingresos
• Negocios que requieren reportes detallados

🔒 PRIVACIDAD Y SEGURIDAD:
• Los datos se almacenan localmente
• No se envía información a servidores externos
• Acceso solo a notificaciones de Yape
• Cumple con políticas de privacidad

📱 REQUISITOS:
• Android 7.0 (API 24) o superior
• Permisos de acceso a notificaciones
• Aplicación Yape instalada

¡Simplifica el control de tus negocios con YapeHub!
```

### Descripción Corta:
```
Captura y organiza notificaciones de Yape para generar reportes detallados de tus negocios
```

### Palabras Clave:
```
yape, notificaciones, reportes, negocios, finanzas, emprendedores, transacciones, ingresos
```

## 🖼️ Paso 5: Subir Assets Gráficos

### Íconos:
1. **Ícono de aplicación**: Subir ícono principal
2. **Ícono de funcionalidad**: Para Play Store

### Screenshots:
1. Subir mínimo 2 screenshots de teléfono
2. Agregar descripciones a cada screenshot
3. Opcional: Screenshots de tablet

### Banner de Funcionalidad:
- **Tamaño**: 1024x500 px
- **Formato**: PNG o JPG
- **Contenido**: Logo + texto descriptivo

## 📦 Paso 6: Subir el AAB

### 1. **Ir a Producción**
- En el menú lateral: "Producción"
- Clic en "Crear nueva versión"

### 2. **Subir AAB**
- Arrastra el archivo `composeApp-release.aab`
- O usa "Examinar archivos"
- Espera la verificación automática

### 3. **Configurar Versión**
```
Nombre de la versión: 1.0.0
Código de versión: 1 (automático)
```

### 4. **Notas de la Versión**
```
Versión inicial de YapeHub

✨ Nuevas funcionalidades:
• Captura automática de notificaciones de Yape
• Organización por negocios
• Reportes detallados
• Exportación CSV y PDF
• Interfaz intuitiva

🔧 Características técnicas:
• Compatible con Android 7.0+
• Almacenamiento local seguro
• Sin conexión a internet requerida
```

## 🔍 Paso 7: Configurar Contenido de la Aplicación

### 1. **Clasificación de Contenido**
- Completar cuestionario de clasificación
- Seleccionar categoría: **Finanzas**
- Audiencia objetivo: **Adultos**

### 2. **Audiencia Objetivo**
- **Edad mínima**: 18 años
- **Audiencia**: Adultos interesados en finanzas

### 3. **Política de Privacidad**
- URL de política de privacidad (requerida)
- Ejemplo: `https://tu-sitio.com/privacy-policy`

### 4. **Permisos de la Aplicación**
- Revisar permisos automáticamente detectados
- Justificar permisos sensibles:
  - **Notificaciones**: Para capturar notificaciones de Yape
  - **Almacenamiento**: Para exportar reportes

## 🚦 Paso 8: Configurar Distribución

### 1. **Países y Regiones**
- **Recomendado**: Solo Perú (mercado objetivo)
- **Opcional**: Latinoamérica

### 2. **Precios**
- **Tipo**: Aplicación gratuita
- **Compras dentro de la aplicación**: No

### 3. **Disponibilidad**
- **Fecha de lanzamiento**: Inmediata tras aprobación
- **Dispositivos**: Todos los dispositivos compatibles

## ✅ Paso 9: Revisión y Envío

### 1. **Revisar Todo**
- Verificar información de la aplicación
- Confirmar assets gráficos
- Revisar descripción y keywords
- Verificar configuración de distribución

### 2. **Enviar para Revisión**
- Clic en "Enviar para revisión"
- Confirmar envío
- Esperar notificación de Google

### 3. **Tiempos de Revisión**
- **Primera revisión**: 1-3 días
- **Actualizaciones**: Pocas horas
- **Revisiones adicionales**: Si hay problemas

## 📊 Paso 10: Monitoreo Post-Lanzamiento

### Métricas Importantes:
- **Instalaciones**: Número de descargas
- **Calificaciones**: Rating promedio
- **Reseñas**: Comentarios de usuarios
- **Crashes**: Errores reportados

### Herramientas de Monitoreo:
- **Play Console**: Estadísticas básicas
- **Firebase Analytics**: Análisis detallado
- **Crashlytics**: Reporte de errores

## 🔄 Actualizaciones Futuras

### Para Actualizar la App:
```bash
# 1. Incrementar versión en build.gradle.kts
versionCode = 2
versionName = "1.1.0"

# 2. Generar nuevo AAB
./scripts/build-bundle.sh

# 3. Subir a Play Console
# 4. Agregar notas de la versión
# 5. Enviar para revisión
```

## 🚨 Problemas Comunes y Soluciones

### Error: "AAB no firmado"
```bash
# Verificar configuración de firma
./gradlew :composeApp:signingReport

# Regenerar AAB
./scripts/build-bundle.sh
```

### Error: "Permisos no justificados"
- Revisar AndroidManifest.xml
- Justificar cada permiso en Play Console
- Eliminar permisos innecesarios

### Error: "Política de privacidad requerida"
- Crear página de política de privacidad
- Agregar URL en Play Console

### Rechazo por "Contenido repetitivo"
- Mejorar descripción única
- Agregar características distintivas
- Diferenciarse de apps similares

## 📞 Recursos Adicionales

### Documentación Oficial:
- [Guía de Play Console](https://support.google.com/googleplay/android-developer/)
- [Políticas de Google Play](https://play.google.com/about/developer-policy/)
- [Mejores prácticas](https://developer.android.com/distribute/best-practices)

### Herramientas Útiles:
- [App Bundle Explorer](https://developer.android.com/studio/command-line/bundletool)
- [Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)
- [Screenshot Generator](https://www.appmockup.com/)

---

**🎉 ¡Tu aplicación YapeHub estará pronto en Google Play Store!** 🚀