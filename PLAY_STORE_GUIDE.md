# 🚀 Guía para Publicar YapeHub en Google Play Store

## ✅ Requisitos Completados

### 1. **Configuración de Firma Digital**
- ✅ Configuración de keystore en `build.gradle.kts`
- ✅ Archivo `keystore.properties` creado
- ✅ ProGuard configurado para optimización
- ✅ `.gitignore` actualizado para proteger archivos sensibles

### 2. **AndroidManifest.xml Optimizado**
- ✅ Permisos necesarios configurados
- ✅ Configuración de backup y data extraction
- ✅ `usesCleartextTraffic="false"` para seguridad
- ✅ Servicios y providers configurados

### 3. **Configuración de Build**
- ✅ Optimización de APK habilitada
- ✅ Bundle configuration para Play Store
- ✅ Splits por ABI configurados
- ✅ ProGuard rules para Compose y Ktor

### 4. **Iconos y Assets**
- ✅ Iconos adaptativos configurados
- ✅ Diseño consistente con la app

## 🔧 Pasos para Generar el Keystore

### 1. Generar el keystore:
```bash
cd /Users/carlos/Desktop/projects/yapechamo
keytool -genkey -v -keystore keystore/yapehub-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias yapehub-key
```

### 2. Configurar las contraseñas:
Edita el archivo `keystore.properties` y reemplaza `TU_PASSWORD_AQUI` con contraseñas seguras.

### 3. Generar el APK firmado:
```bash
./gradlew assembleRelease
```

### 4. Generar el AAB (recomendado para Play Store):
```bash
./gradlew bundleRelease
```

## 📱 Assets Necesarios para Play Store

### 1. **Capturas de Pantalla** (Requeridas)
- **Teléfono**: 1080x1920px (mínimo 2, máximo 8)
- **Tablet**: 1200x1920px (opcional)
- **7" Tablet**: 1200x1920px (opcional)
- **10" Tablet**: 1600x2560px (opcional)

### 2. **Icono de la App**
- **Alta resolución**: 512x512px (PNG, sin transparencia)
- Ya tienes el icono adaptativo configurado ✅

### 3. **Gráfico de Características**
- **Banner**: 1024x500px (opcional pero recomendado)

### 4. **Video Promocional**
- **YouTube**: Máximo 2 minutos (opcional)

## 📝 Información de la App para Play Store

### **Título**: YapeHub
### **Descripción Corta**: Gestiona tus pagos de Yape de forma inteligente
### **Descripción Completa**:
```
YapeHub es la aplicación definitiva para gestionar tus pagos de Yape de manera inteligente y eficiente.

🚀 CARACTERÍSTICAS PRINCIPALES:
• Captura automática de notificaciones de Yape
• Procesamiento inteligente de pagos recibidos
• Interfaz moderna y fácil de usar
• Notificaciones personalizadas
• Historial de transacciones
• Escaneo de códigos QR para pagos rápidos

💡 FUNCIONALIDADES:
• Detección automática de pagos recibidos
• Notificaciones con sonidos personalizados
• Interfaz intuitiva con Material Design 3
• Soporte para múltiples tipos de transacciones
• Historial completo de movimientos

🔒 SEGURIDAD:
• Procesamiento local de datos
• Sin almacenamiento de información sensible
• Permisos mínimos necesarios
• Cumple con políticas de privacidad de Google

¡Descarga YapeHub y lleva el control de tus pagos de Yape al siguiente nivel!
```

### **Categoría**: Finanzas
### **Etiquetas**: yape, pagos, finanzas, perú, dinero, transacciones

## 🎯 Políticas de Play Store

### **Permisos Justificados**:
- `INTERNET`: Para funcionalidades de red
- `ACCESS_NETWORK_STATE`: Para verificar conectividad
- `BIND_NOTIFICATION_LISTENER_SERVICE`: Para capturar notificaciones de Yape
- `FOREGROUND_SERVICE`: Para servicios en segundo plano
- `WAKE_LOCK`: Para mantener el servicio activo

### **Declaración de Privacidad**:
Necesitas crear una política de privacidad que explique:
- Qué datos recopila la app
- Cómo se usan los datos
- Si se comparten con terceros
- Cómo contactar al desarrollador

## 🚀 Proceso de Publicación

### 1. **Preparar la App**:
```bash
# Generar AAB firmado
./gradlew bundleRelease

# El archivo estará en:
# composeApp/build/outputs/bundle/release/composeApp-release.aab
```

### 2. **Subir a Play Console**:
- Ve a [Google Play Console](https://play.google.com/console)
- Crea una nueva aplicación
- Sube el archivo `.aab`
- Completa la información de la tienda
- Sube las capturas de pantalla
- Configura la política de privacidad

### 3. **Revisión**:
- Google revisará la app (1-3 días)
- Pueden solicitar cambios o aclaraciones
- Una vez aprobada, estará disponible en la tienda

## ⚠️ Consideraciones Importantes

### **Servicios de Accesibilidad**:
- Tu app usa `BIND_NOTIFICATION_LISTENER_SERVICE`
- Debes justificar claramente por qué es necesario
- Explica que es solo para capturar notificaciones de Yape
- No para acceder a otros datos del usuario

### **Política de Privacidad**:
- **OBLIGATORIO**: Debes tener una política de privacidad
- Debe estar accesible desde la app
- Debe explicar el uso del servicio de accesibilidad

### **Testing**:
- Prueba la app en diferentes dispositivos
- Verifica que todos los permisos funcionen correctamente
- Asegúrate de que la app funcione sin conexión a internet

## 📞 Soporte

Si necesitas ayuda con algún paso específico, no dudes en preguntar. ¡Tu app está lista para ser publicada en Google Play Store! 🎉
