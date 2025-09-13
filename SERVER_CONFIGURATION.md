# Configuración del Servidor - YapeChamo

## Problema Resuelto: Cleartext HTTP Traffic

El error `cleartext HTTP traffic not permitted` se ha solucionado con dos cambios:

### ✅ **1. Permitir HTTP en Android**
- ✅ **AndroidManifest.xml actualizado** con `android:usesCleartextTraffic="true"`
- ✅ **Permite tráfico HTTP** para desarrollo local

### ✅ **2. Configuración Centralizada**
- ✅ **ServerConfig.kt creado** para fácil configuración
- ✅ **HttpService actualizado** para usar la configuración

## 🔧 **Cómo Configurar tu IP**

### **Paso 1: Encontrar tu IP**

#### **En Windows:**
```cmd
ipconfig
```
Busca la línea `IPv4 Address` de tu conexión activa (ej: `192.168.1.100`)

#### **En macOS/Linux:**
```bash
ifconfig
# o
ip addr show
```

### **Paso 2: Actualizar la Configuración**

Edita el archivo `composeApp/src/commonMain/kotlin/org/sysarp/project/config/ServerConfig.kt`:

```kotlin
object ServerConfig {
    // Cambia esta línea por tu IP real:
    const val BASE_URL = "http://TU_IP_AQUI:8080/api"
    
    // Ejemplos:
    // const val BASE_URL = "http://192.168.1.100:8080/api"  // IP local
    // const val BASE_URL = "http://10.0.2.2:8080/api"       // Emulador Android
    // const val BASE_URL = "http://192.168.0.105:8080/api"  // Otra IP local
}
```

### **Paso 3: Verificar que tu Servidor esté Accesible**

Asegúrate de que tu servidor backend esté corriendo y sea accesible desde la IP que configuraste:

```bash
# Probar desde tu computadora:
curl http://TU_IP:8080/api/auth/admin/register

# O desde el navegador:
http://TU_IP:8080/api/auth/admin/register
```

## 📱 **Configuraciones por Dispositivo**

### **Emulador Android:**
```kotlin
const val BASE_URL = "http://10.0.2.2:8080/api"
```

### **Dispositivo Físico (misma red WiFi):**
```kotlin
const val BASE_URL = "http://192.168.1.100:8080/api"  // Tu IP real
```

### **Servidor en la Nube:**
```kotlin
const val BASE_URL = "https://tu-servidor.com/api"  // HTTPS recomendado
```

## 🔍 **Troubleshooting**

### **Si aún tienes problemas de conexión:**

1. **Verifica el firewall:**
   ```bash
   # En Windows, permite el puerto 8080
   netsh advfirewall firewall add rule name="YapeChamo Backend" dir=in action=allow protocol=TCP localport=8080
   ```

2. **Verifica que el servidor esté corriendo:**
   ```bash
   netstat -an | grep 8080
   ```

3. **Prueba la conectividad:**
   ```bash
   telnet TU_IP 8080
   ```

### **Logs de Debug:**

La app ahora mostrará en los logs:
```
Validando formulario:
businessName: 'Mi Negocio' (isNotBlank: true)
businessType: 'RESTAURANT' (isNotBlank: true, inValidTypes: true)
...
Validación final: VÁLIDA
```

Y también logs de Ktor para las peticiones HTTP.

## 🚀 **Para Producción**

Cuando estés listo para producción:

1. **Cambiar a HTTPS:**
   ```kotlin
   const val BASE_URL = "https://tu-dominio.com/api"
   ```

2. **Remover cleartext traffic:**
   ```xml
   <!-- En AndroidManifest.xml, cambiar: -->
   android:usesCleartextTraffic="false"
   ```

3. **Agregar certificados SSL** si es necesario.

## 📋 **Checklist de Configuración**

- [ ] ✅ Encontrar tu IP local
- [ ] ✅ Actualizar `ServerConfig.kt` con tu IP
- [ ] ✅ Verificar que el servidor backend esté corriendo
- [ ] ✅ Probar conectividad con curl/navegador
- [ ] ✅ Compilar y probar la app
- [ ] ✅ Verificar logs de debug

## 🎯 **Ejemplo Completo**

Si tu IP es `192.168.1.105` y tu servidor corre en puerto `8080`:

```kotlin
// ServerConfig.kt
object ServerConfig {
    const val BASE_URL = "http://192.168.1.105:8080/api"
    const val TIMEOUT_SECONDS = 15L
    const val MAX_RETRIES = 3
}
```

```bash
# Probar conectividad:
curl http://192.168.1.105:8080/api/auth/admin/register
```

¡Con estos cambios, tu app debería poder conectarse correctamente a tu servidor backend!
