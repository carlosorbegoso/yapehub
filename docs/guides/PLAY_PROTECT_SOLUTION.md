# 🛡️ Solución: Google Play Protect Bloquea YapeHub

## 🚨 Problema
Google Play Protect bloquea YapeHub con el mensaje:
> "Aplicación bloqueada para proteger tu dispositivo"

## ✅ Soluciones

### 🎯 **Solución 1: Permitir YapeHub (RECOMENDADO)**

#### Paso 1: En el diálogo de bloqueo
1. **Toca "Entendido"** (no canceles)
2. **Anota** que YapeHub fue bloqueada

#### Paso 2: Ir a configuración de Play Protect
1. **Abre Google Play Store**
2. **Toca tu foto de perfil** (esquina superior derecha)
3. **Selecciona "Play Protect"**
4. **Toca "Configuración"** (ícono de engranaje)

#### Paso 3: Permitir YapeHub
1. **Busca "Aplicaciones bloqueadas"** o **"Aplicaciones rechazadas"**
2. **Encuentra "YapeHub"** en la lista
3. **Toca "Permitir"** o **"Instalar de todos modos"**
4. **Confirma** la acción

#### Paso 4: Instalar YapeHub
1. **Regresa** al archivo APK
2. **Toca para instalar**
3. **Debería instalarse** sin problemas

---

### 🔧 **Solución 2: Desactivar Play Protect Temporalmente**

#### Paso 1: Desactivar protección
1. **Google Play Store** → **Perfil** → **Play Protect**
2. **Configuración** (ícono engranaje)
3. **Desactiva**: "Analizar aplicaciones con Play Protect"
4. **Confirma** desactivación

#### Paso 2: Instalar YapeHub
1. **Instala** el APK normalmente
2. **Otorga** permisos necesarios

#### Paso 3: Reactivar protección
1. **Regresa** a Play Protect
2. **Reactiva**: "Analizar aplicaciones con Play Protect"
3. **YapeHub** quedará como excepción

---

### 💻 **Solución 3: Instalación vía ADB (Desarrolladores)**

#### Requisitos:
- Android SDK instalado
- Depuración USB habilitada
- Cable USB

#### Comandos:
```bash
# Verificar dispositivo conectado
adb devices

# Instalar YapeHub
adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk

# Si hay error, desinstalar primero
adb uninstall com.yapechamo.composeapp
adb install composeApp/build/outputs/apk/release/composeApp-release.apk
```

---

## 🔍 **¿Por qué Google bloquea YapeHub?**

### Razones técnicas:
1. **APK externa** (no de Play Store)
2. **Permisos sensibles**:
   - Acceso a notificaciones
   - Posible acceso a datos financieros
3. **Aplicación nueva** (sin historial de seguridad)

### ¿Es seguro permitir YapeHub?
✅ **SÍ es seguro** porque:
- Es **tu propia aplicación**
- **Código fuente disponible**
- **No envía datos** a servidores externos
- **Solo lee notificaciones** de Yape localmente

---

## 🚀 **Después de Instalar YapeHub**

### 1. **Configurar permisos**
1. **Configuración** → **Aplicaciones** → **YapeHub**
2. **Permisos** → **Permitir acceso a notificaciones**
3. **Configuración** → **Accesibilidad** → **YapeHub** → **Activar**

### 2. **Verificar funcionamiento**
1. **Abre YapeHub**
2. **Presiona "Iniciar Captura"**
3. **Envía/recibe** un pago de prueba con Yape
4. **Verifica** que se capture la notificación

---

## 🛡️ **Prevenir Futuros Bloqueos**

### Para usuarios finales:
1. **Distribuir vía Play Store** (solución definitiva)
2. **Firmar con certificado verificado**
3. **Incluir política de privacidad**

### Para desarrollo:
1. **Usar keystore consistente**
2. **Mantener mismo package name**
3. **Documentar permisos necesarios**

---

## 📞 **Si Sigues Teniendo Problemas**

### Alternativas:
1. **Instalar en modo desarrollador**
2. **Usar emulador Android**
3. **Probar en dispositivo diferente**
4. **Contactar soporte** si es para distribución

### Comandos de diagnóstico:
```bash
# Verificar APK
./scripts/verify-apk.sh

# Verificar firma
./gradlew :composeApp:signingReport

# Generar nueva APK
./scripts/build-all.sh -t apk
```

---

**💡 Recuerda: Este bloqueo es normal para APKs externas con permisos sensibles. YapeHub es segura de usar.**