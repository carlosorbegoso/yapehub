# Conectividad Emulador Android ↔ Máquina Host

## 🎯 **Tu Situación Específica**
- ✅ **App**: Corre en **emulador Android**
- ✅ **Backend**: Corre en tu **máquina normal** (host)
- ✅ **Solución**: Usar IP especial `10.0.2.2`

## 🔧 **Configuración Correcta**

### **ServerConfig.kt ya configurado:**
```kotlin
object ServerConfig {
    // Para desarrollo local con EMULADOR ANDROID:
    const val BASE_URL = "http://10.0.2.2:8080/api"  // IP del host desde emulador Android
}
```

## 📱 **Cómo Funciona**

### **Mapeo de IPs en Emulador Android:**
- `10.0.2.2` → Tu máquina host (donde está el backend)
- `10.0.2.1` → Gateway del emulador
- `10.0.2.3` → Servidor DNS del emulador
- `127.0.0.1` → El propio emulador (NO tu máquina)

## ✅ **Verificaciones Necesarias**

### **1. Verificar que tu Backend esté Corriendo**
```bash
# En tu máquina normal, verifica que el puerto 8080 esté abierto:
netstat -an | grep 8080

# Debería mostrar algo como:
# tcp4  0  0  *.8080  *.*  LISTEN
```

### **2. Probar desde tu Máquina**
```bash
# Desde tu máquina normal:
curl http://localhost:8080/api/auth/admin/register

# Debería responder con un error de validación (eso está bien):
# {"message": "Validation failed", "code": "VALIDATION_ERROR", ...}
```

### **3. Verificar Firewall**
```bash
# Windows - Permitir puerto 8080:
netsh advfirewall firewall add rule name="YapeChamo Backend" dir=in action=allow protocol=TCP localport=8080

# macOS - Verificar que no esté bloqueado:
sudo lsof -i :8080
```

## 🚀 **Probar la Conexión**

### **Paso 1: Asegúrate de que tu Backend esté Corriendo**
```bash
# En tu máquina normal, inicia tu servidor backend
# Debería estar escuchando en puerto 8080
```

### **Paso 2: Probar desde el Emulador**
```bash
# Desde el emulador Android (usando adb):
adb shell
curl http://10.0.2.2:8080/api/auth/admin/register

# Debería responder con el mismo error de validación
```

### **Paso 3: Probar la App**
1. **Abre la app** en el emulador
2. **Ve al registro** de administrador
3. **Completa el formulario** con datos válidos
4. **Presiona "Registrar Administrador"**
5. **Debería conectarse** sin error EPREM

## 🔍 **Troubleshooting**

### **Si aún tienes problemas:**

#### **1. Verificar que el Backend esté Accesible**
```bash
# Desde tu máquina:
curl http://localhost:8080/api/auth/admin/register

# Si no responde, tu backend no está corriendo correctamente
```

#### **2. Verificar Firewall**
```bash
# Windows:
netsh advfirewall firewall show rule name="YapeChamo Backend"

# macOS:
sudo pfctl -s rules | grep 8080
```

#### **3. Verificar Puerto**
```bash
# Verificar que el puerto esté abierto:
telnet localhost 8080

# Si funciona, deberías ver:
# Trying 127.0.0.1...
# Connected to localhost.
# Escape character is '^]'.
```

## 📋 **Checklist Final**

- [ ] ✅ Backend corriendo en puerto 8080 en tu máquina
- [ ] ✅ Firewall permite conexiones en puerto 8080
- [ ] ✅ `ServerConfig.kt` configurado con `http://10.0.2.2:8080/api`
- [ ] ✅ App compilada y instalada en emulador
- [ ] ✅ Probar registro desde la app

## 🎯 **Mensajes Esperados**

### **Si funciona correctamente:**
- ✅ **Validación pasa** (sin mensaje de error de validación)
- ✅ **Petición HTTP se envía** a `http://10.0.2.2:8080/api/auth/admin/register`
- ✅ **Respuesta del servidor** (éxito o error de validación del backend)

### **Si hay problemas:**
- ❌ **"Error de conectividad: No se puede conectar al servidor"** → Backend no está corriendo
- ❌ **"Conexión rechazada"** → Firewall bloqueando o puerto incorrecto
- ❌ **"Timeout"** → Backend muy lento o no responde

## 🚀 **¡Listo para Probar!**

Con `10.0.2.2:8080` configurado, tu app del emulador debería poder conectarse perfectamente a tu backend en la máquina host.

¿Tu backend está corriendo en puerto 8080? ¿Puedes probarlo ahora?
