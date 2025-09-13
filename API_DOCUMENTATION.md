# Documentación de APIs - Sistema YapeChamo

## Índice
1. [Autenticación y Usuarios](#autenticación-y-usuarios)
2. [Gestión de Administradores](#gestión-de-administradores)
3. [Gestión de Vendedores](#gestión-de-vendedores)
4. [Transacciones Yape](#transacciones-yape)
5. [Reportes y Analytics](#reportes-y-analytics)
6. [Notificaciones](#notificaciones)
7. [Códigos QR y Afiliación](#códigos-qr-y-afiliación)

---

## Autenticación y Usuarios

### 1. Registro de Administrador
```http
POST /api/auth/admin/register
Content-Type: application/json

{
  "businessName": "Mi Negocio SRL",
  "businessType": "RESTAURANT",
  "ruc": "20123456789",
  "email": "admin@minegocio.com",
  "password": "SecurePass123!",
  "phone": "+51987654321",
  "address": "Av. Principal 123, Lima",
  "contactName": "Juan Pérez"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Administrador registrado exitosamente",
  "data": {
    "adminId": "admin_123456",
    "businessId": "business_789012",
    "email": "admin@minegocio.com",
    "businessName": "Mi Negocio SRL",
    "verificationRequired": true
  }
}
```

### 2. Login de Usuario
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@minegocio.com",
  "password": "SecurePass123!",
  "deviceFingerprint": "device_unique_id_123",
  "role": "ADMIN" // o "SELLER"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_123456",
    "expiresIn": 3600,
    "user": {
      "id": "admin_123456",
      "email": "admin@minegocio.com",
      "role": "ADMIN",
      "businessId": "business_789012",
      "businessName": "Mi Negocio SRL",
      "isVerified": true
    }
  }
}
```

### 3. Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "refresh_token_123456"
}
```

### 4. Logout
```http
POST /api/auth/logout
Authorization: Bearer {access_token}
```

### 5. Recuperar Contraseña
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "admin@minegocio.com"
}
```

### 6. Cambiar Contraseña
```http
POST /api/auth/change-password
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!"
}
```

---

## Gestión de Administradores

### 7. Obtener Perfil de Administrador
```http
GET /api/admin/profile
Authorization: Bearer {access_token}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "id": "admin_123456",
    "email": "admin@minegocio.com",
    "businessName": "Mi Negocio SRL",
    "businessType": "RESTAURANT",
    "ruc": "20123456789",
    "phone": "+51987654321",
    "address": "Av. Principal 123, Lima",
    "contactName": "Juan Pérez",
    "isVerified": true,
    "createdAt": "2024-01-15T10:30:00Z",
    "branches": [
      {
        "id": "branch_001",
        "name": "Sucursal Centro",
        "code": "SUC001",
        "address": "Av. Centro 456",
        "isActive": true
      }
    ]
  }
}
```

### 8. Actualizar Perfil de Administrador
```http
PUT /api/admin/profile
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "businessName": "Mi Negocio Actualizado SRL",
  "phone": "+51987654322",
  "address": "Av. Nueva Dirección 789",
  "contactName": "Juan Pérez Actualizado"
}
```

---

## Gestión de Vendedores

### 9. Afiliar Vendedor
```http
POST /api/admin/sellers/affiliate
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "sellerName": "María García",
  "email": "maria@email.com",
  "phone": "+51987654323",
  "branchId": "branch_001",
  "affiliationCode": "AFF_1705312200000_1234"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Vendedor afiliado exitosamente",
  "data": {
    "sellerId": "seller_789012",
    "name": "María García",
    "email": "maria@email.com",
    "branchId": "branch_001",
    "branchName": "Sucursal Centro",
    "isActive": true,
    "affiliationDate": "2024-01-15T14:30:00Z"
  }
}
```

### 10. Listar Vendedores
```http
GET /api/admin/sellers
Authorization: Bearer {access_token}
Query Parameters:
- page: 1
- limit: 20
- branchId: branch_001 (opcional)
- status: active|inactive|all (opcional)
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "sellers": [
      {
        "id": "seller_789012",
        "name": "María García",
        "email": "maria@email.com",
        "phone": "+51987654323",
        "branchId": "branch_001",
        "branchName": "Sucursal Centro",
        "isActive": true,
        "isOnline": false,
        "totalPayments": 45,
        "totalAmount": "1250.75",
        "lastPayment": "2024-01-15T13:45:00Z",
        "affiliationDate": "2024-01-10T09:00:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 3,
      "totalItems": 45,
      "itemsPerPage": 20
    }
  }
}
```

### 11. Actualizar Vendedor
```http
PUT /api/admin/sellers/{sellerId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "name": "María García Actualizada",
  "phone": "+51987654324",
  "isActive": true
}
```

### 12. Eliminar/Pausar Vendedor
```http
DELETE /api/admin/sellers/{sellerId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "action": "pause", // o "delete"
  "reason": "Motivo de la pausa/eliminación"
}
```

### 13. Solicitudes de Desactivación
```http
GET /api/admin/deactivation-requests
Authorization: Bearer {access_token}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "requests": [
      {
        "id": "req_123456",
        "sellerId": "seller_789012",
        "sellerName": "María García",
        "reason": "Cambio de trabajo",
        "requestDate": "2024-01-15T10:00:00Z",
        "status": "pending"
      }
    ]
  }
}
```

### 14. Procesar Solicitud de Desactivación
```http
POST /api/admin/deactivation-requests/{requestId}/process
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "action": "approve", // o "reject"
  "adminNotes": "Solicitud aprobada"
}
```

---

## Transacciones Yape

### 15. Obtener Transacciones
```http
GET /api/transactions
Authorization: Bearer {access_token}
Query Parameters:
- page: 1
- limit: 50
- startDate: 2024-01-01
- endDate: 2024-01-31
- branchId: branch_001 (opcional)
- sellerId: seller_789012 (opcional)
- status: pending|confirmed|all (opcional)
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "transactions": [
      {
        "id": "txn_123456",
        "securityCode": "ABC123",
        "amount": "25.50",
        "timestamp": "2024-01-15T14:30:00Z",
        "description": "Pago de almuerzo",
        "type": "PAYMENT",
        "businessName": "Mi Negocio SRL",
        "branchId": "branch_001",
        "branchName": "Sucursal Centro",
        "sellerId": "seller_789012",
        "sellerName": "María García",
        "isProcessed": false,
        "paymentMethod": "YAPE",
        "customerPhone": "+51987654325"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 95,
      "itemsPerPage": 50
    }
  }
}
```

### 16. Confirmar Transacción
```http
POST /api/transactions/{transactionId}/confirm
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "sellerId": "seller_789012",
  "notes": "Transacción confirmada manualmente"
}
```

### 17. Marcar Transacción como Procesada
```http
POST /api/transactions/{transactionId}/process
Authorization: Bearer {access_token}
```

### 18. Obtener Transacciones por Vendedor
```http
GET /api/sellers/{sellerId}/transactions
Authorization: Bearer {access_token}
Query Parameters:
- page: 1
- limit: 20
- startDate: 2024-01-01
- endDate: 2024-01-31
```

---

## Reportes y Analytics

### 19. Dashboard de Administrador
```http
GET /api/admin/dashboard
Authorization: Bearer {access_token}
Query Parameters:
- period: today|week|month|year
- branchId: branch_001 (opcional)
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalTransactions": 1250,
      "totalAmount": "45678.90",
      "pendingTransactions": 15,
      "activeSellers": 8,
      "totalBranches": 3
    },
    "dailyStats": [
      {
        "date": "2024-01-15",
        "transactions": 45,
        "amount": "1250.75"
      }
    ],
    "topSellers": [
      {
        "sellerId": "seller_789012",
        "sellerName": "María García",
        "transactions": 45,
        "amount": "1250.75"
      }
    ],
    "branchStats": [
      {
        "branchId": "branch_001",
        "branchName": "Sucursal Centro",
        "transactions": 85,
        "amount": "2150.25"
      }
    ]
  }
}
```

### 20. Dashboard de Vendedor
```http
GET /api/sellers/dashboard
Authorization: Bearer {access_token}
Query Parameters:
- period: today|week|month
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "sellerInfo": {
      "id": "seller_789012",
      "name": "María García",
      "branchName": "Sucursal Centro",
      "isActive": true
    },
    "summary": {
      "totalTransactions": 45,
      "totalAmount": "1250.75",
      "pendingTransactions": 3,
      "todayTransactions": 8,
      "todayAmount": "225.50"
    },
    "recentTransactions": [
      {
        "id": "txn_123456",
        "amount": "25.50",
        "timestamp": "2024-01-15T14:30:00Z",
        "isProcessed": false
      }
    ]
  }
}
```

### 21. Reportes de Analytics
```http
GET /api/admin/analytics/reports
Authorization: Bearer {access_token}
Query Parameters:
- type: daily|weekly|monthly|yearly
- startDate: 2024-01-01
- endDate: 2024-01-31
- branchId: branch_001 (opcional)
```

### 22. Exportar Transacciones
```http
GET /api/admin/transactions/export
Authorization: Bearer {access_token}
Query Parameters:
- format: csv|excel|pdf
- startDate: 2024-01-01
- endDate: 2024-01-31
- branchId: branch_001 (opcional)
```

---

## Notificaciones

### 23. Enviar Notificación Push
```http
POST /api/notifications/send
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "targetType": "seller", // o "admin", "all"
  "targetId": "seller_789012", // opcional si es "all"
  "title": "Nueva transacción",
  "message": "Tienes una nueva transacción pendiente",
  "type": "transaction",
  "data": {
    "transactionId": "txn_123456",
    "amount": "25.50"
  }
}
```

### 24. Obtener Notificaciones
```http
GET /api/notifications
Authorization: Bearer {access_token}
Query Parameters:
- page: 1
- limit: 20
- unreadOnly: true|false
```

### 25. Marcar Notificación como Leída
```http
POST /api/notifications/{notificationId}/read
Authorization: Bearer {access_token}
```

---

## Códigos QR y Afiliación

### 26. Generar Código QR
```http
POST /api/admin/qr/generate
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "type": "affiliation",
  "expirationHours": 24,
  "maxUses": 10,
  "branchId": "branch_001"
}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "qrId": "qr_123456",
    "qrCode": "QR_DATA_ENCODED",
    "qrImageUrl": "https://api.yapechamo.com/qr/qr_123456.png",
    "expiresAt": "2024-01-16T14:30:00Z",
    "maxUses": 10,
    "remainingUses": 10,
    "branchId": "branch_001"
  }
}
```

### 27. Generar Código de Afiliación
```http
POST /api/admin/affiliation-codes/generate
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "expirationHours": 72,
  "maxUses": 1,
  "branchId": "branch_001",
  "notes": "Código para nuevo vendedor"
}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "affiliationCode": "AFF_1705312200000_1234",
    "expiresAt": "2024-01-18T14:30:00Z",
    "maxUses": 1,
    "remainingUses": 1,
    "branchId": "branch_001"
  }
}
```

### 28. Validar Código de Afiliación
```http
POST /api/auth/validate-affiliation-code
Content-Type: application/json

{
  "affiliationCode": "AFF_1705312200000_1234"
}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "isValid": true,
    "businessName": "Mi Negocio SRL",
    "branchName": "Sucursal Centro",
    "expiresAt": "2024-01-18T14:30:00Z"
  }
}
```

### 29. Obtener Códigos Activos
```http
GET /api/admin/qr/active
Authorization: Bearer {access_token}
```

### 30. Revocar Código QR/Afiliación
```http
DELETE /api/admin/qr/{qrId}
Authorization: Bearer {access_token}
```

---

## Códigos de Estado HTTP

- **200 OK**: Solicitud exitosa
- **201 Created**: Recurso creado exitosamente
- **400 Bad Request**: Datos de entrada inválidos
- **401 Unauthorized**: Token inválido o expirado
- **403 Forbidden**: Sin permisos para la operación
- **404 Not Found**: Recurso no encontrado
- **409 Conflict**: Conflicto (ej: email ya existe)
- **422 Unprocessable Entity**: Datos válidos pero no procesables
- **500 Internal Server Error**: Error interno del servidor

---

## Autenticación

Todas las rutas protegidas requieren el header:
```
Authorization: Bearer {access_token}
```

Los tokens JWT expiran en 1 hora y deben renovarse usando el refresh token.

---

## Rate Limiting

- **Límite general**: 1000 requests por hora por IP
- **Login**: 5 intentos por minuto por IP
- **Registro**: 3 intentos por minuto por IP
- **APIs críticas**: 100 requests por minuto por usuario

---

## Webhooks (Opcional)

### Notificación de Nueva Transacción
```http
POST /webhooks/yape-transaction
Content-Type: application/json

{
  "event": "transaction.created",
  "timestamp": "2024-01-15T14:30:00Z",
  "data": {
    "transactionId": "txn_123456",
    "securityCode": "ABC123",
    "amount": "25.50",
    "businessId": "business_789012",
    "branchId": "branch_001"
  }
}
```

---

## Ejemplos de Implementación

### Backend Node.js/Express
```javascript
// Ejemplo de endpoint de login
app.post('/api/auth/login', async (req, res) => {
  try {
    const { email, password, deviceFingerprint, role } = req.body;
    
    // Validar datos
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Email y contraseña son requeridos'
      });
    }
    
    // Buscar usuario
    const user = await User.findOne({ email, role });
    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Credenciales inválidas'
      });
    }
    
    // Verificar contraseña
    const isValidPassword = await bcrypt.compare(password, user.password);
    if (!isValidPassword) {
      return res.status(401).json({
        success: false,
        message: 'Credenciales inválidas'
      });
    }
    
    // Generar tokens
    const accessToken = jwt.sign(
      { userId: user.id, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );
    
    const refreshToken = jwt.sign(
      { userId: user.id },
      process.env.REFRESH_SECRET,
      { expiresIn: '7d' }
    );
    
    res.json({
      success: true,
      message: 'Login exitoso',
      data: {
        accessToken,
        refreshToken,
        expiresIn: 3600,
        user: {
          id: user.id,
          email: user.email,
          role: user.role,
          businessId: user.businessId,
          businessName: user.businessName,
          isVerified: user.isVerified
        }
      }
    });
    
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});
```

### Frontend Kotlin (Android)
```kotlin
// Ejemplo de servicio de autenticación
class AuthService {
    suspend fun login(email: String, password: String, role: String): LoginResponse {
        val request = LoginRequest(
            email = email,
            password = password,
            deviceFingerprint = getDeviceFingerprint(),
            role = role
        )
        
        return apiClient.post("/api/auth/login", request)
    }
    
    suspend fun refreshToken(refreshToken: String): RefreshResponse {
        val request = RefreshRequest(refreshToken = refreshToken)
        return apiClient.post("/api/auth/refresh", request)
    }
}
```

---

Esta documentación cubre todos los endpoints necesarios para implementar el sistema completo. ¿Te gustaría que profundice en alguna sección específica o que agregue más detalles sobre algún endpoint en particular?
