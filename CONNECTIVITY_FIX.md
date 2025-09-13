# Solución al Error EPREM (Operation Not Permitted)

## ✅ **Problema Identificado**
- **Error**: `EPREM (operation not permitted)`
- **Causa**: Problema de conectividad de red
- **Solución**: Configurar la IP correcta del servidor

## 🔧 **Pasos para Solucionar**

### **Paso 1: Encontrar tu IP**
```bash
# En Windows:
ipconfig

# En macOS/Linux:
ifconfig
```

Busca tu IP local (ej: `192.168.1.105`)

### **Paso 2: Actualizar ServerConfig.kt**
Edita `composeApp/src/commonMain/kotlin/org/sysarp/project/config/ServerConfig.kt`:

```kotlin
object ServerConfig {
    // Cambia esta línea por tu IP real:
    const val BASE_URL = "http://TU_IP_AQUI:8080/api"
    
    // Ejemplo si tu IP es 192.168.1.105:
    // const val BASE_URL = "http://192.168.1.105:8080/api"
}
```

### **Paso 3: Verificar que tu Servidor esté Corriendo**
```bash
# Probar desde tu computadora:
curl http://TU_IP:8080/api/auth/admin/register

# Debería responder algo como:
# {"message": "Validation failed", "code": "VALIDATION_ERROR", ...}
```

## 📱 **Configuraciones por Dispositivo**

### **Emulador Android:**
```kotlin
const val BASE_URL = "http://10.0.2.2:8080/api"
```

### **Dispositivo Físico (misma WiFi):**
```kotlin
const val BASE_URL = "http://192.168.1.105:8080/api"  // Tu IP real
```

### **Si usas ngrok o túnel:**
```kotlin
const val BASE_URL = "http://abc123.ngrok.io/api"
```

## 🔍 **Verificaciones Adicionales**

### **1. Firewall**
Asegúrate de que el puerto 8080 esté abierto:
```bash
# Windows:
netsh advfirewall firewall add rule name="YapeChamo Backend" dir=in action=allow protocol=TCP localport=8080

# macOS:
sudo pfctl -f /etc/pf.conf
```

### **2. Servidor Backend**
Verifica que tu servidor esté corriendo:
```bash
# Verificar puerto:
netstat -an | grep 8080

# Debería mostrar algo como:
# tcp4  0  0  *.8080  *.*  LISTEN
```

### **3. Conectividad**
```bash
# Probar conectividad:
telnet TU_IP 8080

# Si funciona, deberías ver:
# Trying TU_IP...
# Connected to TU_IP.
# Escape character is '^]'.
```

## 🚀 **Mensajes de Error Mejorados**

Ahora la app mostrará mensajes más claros:

- ✅ **"Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."**
- ✅ **"Conexión rechazada: El servidor no está corriendo o no es accesible."**
- ✅ **"Timeout: El servidor tardó demasiado en responder."**
- ✅ **"Red inalcanzable: Verifica tu conexión a internet."**

## 📋 **Checklist de Solución**

- [ ] ✅ Encontrar tu IP local
- [ ] ✅ Actualizar `ServerConfig.kt` con tu IP
- [ ] ✅ Verificar que el servidor backend esté corriendo en puerto 8080
- [ ] ✅ Probar conectividad con curl
- [ ] ✅ Verificar firewall (puerto 8080 abierto)
- [ ] ✅ Compilar y probar la app
- [ ] ✅ Verificar mensaje de error específico

## 🎯 **Ejemplo Completo**

Si tu IP es `192.168.1.105`:

```kotlin
// ServerConfig.kt
object ServerConfig {
    const val BASE_URL = "http://192.168.1.105:8080/api"
}
```

```bash
# Probar:
curl http://192.168.1.105:8080/api/auth/admin/register
```

¡Con estos pasos deberías poder conectarte correctamente a tu servidor!
