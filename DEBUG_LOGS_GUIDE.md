# Guía de Logs de Debug - YapeChamo

## 🔍 **Logs Agregados para Diagnóstico**

He agregado logs detallados para diagnosticar el problema de conectividad. Ahora verás logs específicos cuando intentes registrar un administrador.

## 📱 **Logs que Verás en Android Studio**

### **1. Logs de AuthService (Inicio del proceso):**
```
🚀 [AUTH] Iniciando registro de administrador
📋 [AUTH] Datos recibidos:
   - businessName: 'Mi Empresa'
   - businessType: 'RESTAURANT'
   - ruc: '123345678'
   - email: 'carlos@hotmail.com'
   - phone: '3232343'
   - address: 'fsadfsa'
   - contactName: 'fadfs'
```

### **2. Logs de HttpService (Petición HTTP):**
```
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
📤 [HTTP] Datos enviados: AdminRegistrationRequest(businessName=Mi Empresa, businessType=RESTAURANT, ...)
```

### **3. Logs de Respuesta (Si la conexión funciona):**
```
📥 [HTTP] Respuesta recibida - Status: 400 Bad Request
📥 [HTTP] Headers: [Content-Type: application/json, ...]
❌ [HTTP] Error de validación - Status: 400
📋 [HTTP] Error del servidor: ApiError(message=Validation failed, code=VALIDATION_ERROR, ...)
```

### **4. Logs de Error (Si hay problema de conectividad):**
```
💥 [HTTP] Excepción capturada: ConnectException
💥 [HTTP] Mensaje de error: Connection refused: connect
💥 [HTTP] Stack trace: 
   at io.ktor.client.engine.cio.CIOHttpClient$Engine$request$2.invokeSuspend(CIOHttpClient.kt:...)
   at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:...)
📝 [HTTP] Mensaje de error amigable: Conexión rechazada: El servidor no está corriendo o no es accesible.
```

## 🎯 **Qué Buscar en los Logs**

### **✅ Si Todo Funciona Correctamente:**
```
🚀 [AUTH] Iniciando registro de administrador
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
📥 [HTTP] Respuesta recibida - Status: 400 Bad Request
❌ [HTTP] Error de validación - Status: 400
📋 [HTTP] Error del servidor: ApiError(message=Validation failed, ...)
```

### **❌ Si Hay Problema de Conectividad:**
```
🚀 [AUTH] Iniciando registro de administrador
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
💥 [HTTP] Excepción capturada: ConnectException
💥 [HTTP] Mensaje de error: Connection refused: connect
📝 [HTTP] Mensaje de error amigable: Conexión rechazada: El servidor no está corriendo o no es accesible.
```

### **❌ Si Hay Problema de DNS/Host:**
```
🚀 [AUTH] Iniciando registro de administrador
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
💥 [HTTP] Excepción capturada: UnknownHostException
💥 [HTTP] Mensaje de error: Unable to resolve host "10.0.2.2"
📝 [HTTP] Mensaje de error amigable: Host desconocido: No se puede resolver la dirección del servidor.
```

### **❌ Si Hay Timeout:**
```
🚀 [AUTH] Iniciando registro de administrador
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
💥 [HTTP] Excepción capturada: SocketTimeoutException
💥 [HTTP] Mensaje de error: timeout
📝 [HTTP] Mensaje de error amigable: Timeout: El servidor tardó demasiado en responder.
```

## 🔧 **Cómo Interpretar los Logs**

### **1. Si ves "Connection refused":**
- ❌ **Problema**: Tu backend no está corriendo en puerto 8080
- ✅ **Solución**: Inicia tu servidor backend

### **2. Si ves "UnknownHostException":**
- ❌ **Problema**: El emulador no puede resolver la IP
- ✅ **Solución**: Verificar configuración de red del emulador

### **3. Si ves "SocketTimeoutException":**
- ❌ **Problema**: El servidor está muy lento o no responde
- ✅ **Solución**: Verificar que el backend esté funcionando correctamente

### **4. Si ves "EPREM" o "Operation not permitted":**
- ❌ **Problema**: Permisos de red o firewall
- ✅ **Solución**: Verificar firewall y permisos de red

### **5. Si ves respuesta HTTP (Status 400, 500, etc.):**
- ✅ **¡Buenas noticias!**: La conectividad funciona
- ❌ **Problema**: Error en el backend o datos inválidos
- ✅ **Solución**: Revisar logs del backend

## 📋 **Pasos para Diagnosticar**

1. **Abre Android Studio** y ve a la pestaña "Logcat"
2. **Filtra por tu app**: `com.yapehub.app.debug`
3. **Completa el formulario** de registro
4. **Presiona "Registrar Administrador"**
5. **Observa los logs** que aparecen
6. **Comparte los logs** para diagnóstico específico

## 🎯 **Logs Esperados**

### **Flujo Normal (Con Backend Funcionando):**
```
🚀 [AUTH] Iniciando registro de administrador
📋 [AUTH] Datos recibidos: [todos los campos]
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
📤 [HTTP] Datos enviados: AdminRegistrationRequest(...)
📥 [HTTP] Respuesta recibida - Status: [código HTTP]
```

### **Flujo con Error de Conectividad:**
```
🚀 [AUTH] Iniciando registro de administrador
📋 [AUTH] Datos recibidos: [todos los campos]
🌐 [HTTP] Iniciando petición a: http://10.0.2.2:8080/api/auth/admin/register
💥 [HTTP] Excepción capturada: [tipo de excepción]
💥 [HTTP] Mensaje de error: [mensaje específico]
📝 [HTTP] Mensaje de error amigable: [mensaje para el usuario]
```

¡Ahora prueba el registro y comparte los logs que aparecen para poder diagnosticar exactamente qué está pasando!
