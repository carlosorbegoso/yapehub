# Ejemplos de Implementación de APIs - YapeChamo

## Backend - Node.js + Express + MongoDB

### Estructura del Proyecto
```
backend/
├── src/
│   ├── controllers/
│   │   ├── authController.js
│   │   ├── adminController.js
│   │   ├── sellerController.js
│   │   └── transactionController.js
│   ├── models/
│   │   ├── User.js
│   │   ├── Transaction.js
│   │   ├── Business.js
│   │   └── Branch.js
│   ├── middleware/
│   │   ├── auth.js
│   │   ├── validation.js
│   │   └── rateLimiting.js
│   ├── routes/
│   │   ├── auth.js
│   │   ├── admin.js
│   │   ├── seller.js
│   │   └── transaction.js
│   ├── services/
│   │   ├── authService.js
│   │   ├── notificationService.js
│   │   └── qrService.js
│   └── utils/
│       ├── jwt.js
│       ├── encryption.js
│       └── validation.js
├── package.json
└── .env
```

### Modelos de Base de Datos

#### User.js
```javascript
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
  email: {
    type: String,
    required: true,
    unique: true,
    lowercase: true
  },
  password: {
    type: String,
    required: true,
    minlength: 8
  },
  role: {
    type: String,
    enum: ['ADMIN', 'SELLER'],
    required: true
  },
  businessId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Business',
    required: function() { return this.role === 'ADMIN'; }
  },
  sellerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Seller',
    required: function() { return this.role === 'SELLER'; }
  },
  isVerified: {
    type: Boolean,
    default: false
  },
  isActive: {
    type: Boolean,
    default: true
  },
  lastLogin: Date,
  deviceFingerprints: [String],
  refreshTokens: [String]
}, {
  timestamps: true
});

// Hash password before saving
userSchema.pre('save', async function(next) {
  if (!this.isModified('password')) return next();
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

// Compare password method
userSchema.methods.comparePassword = async function(candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

module.exports = mongoose.model('User', userSchema);
```

#### Transaction.js
```javascript
const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  securityCode: {
    type: String,
    required: true,
    unique: true
  },
  amount: {
    type: Number,
    required: true,
    min: 0.01
  },
  timestamp: {
    type: Date,
    required: true
  },
  description: String,
  type: {
    type: String,
    enum: ['PAYMENT', 'REFUND', 'TRANSFER'],
    default: 'PAYMENT'
  },
  businessId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Business',
    required: true
  },
  branchId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Branch',
    required: true
  },
  sellerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Seller'
  },
  isProcessed: {
    type: Boolean,
    default: false
  },
  processedAt: Date,
  paymentMethod: {
    type: String,
    enum: ['YAPE', 'PLIN', 'BIM'],
    default: 'YAPE'
  },
  customerPhone: String,
  notes: String
}, {
  timestamps: true
});

// Index for efficient queries
transactionSchema.index({ businessId: 1, timestamp: -1 });
transactionSchema.index({ sellerId: 1, timestamp: -1 });
transactionSchema.index({ securityCode: 1 }, { unique: true });

module.exports = mongoose.model('Transaction', transactionSchema);
```

### Controladores

#### authController.js
```javascript
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const Business = require('../models/Business');
const Seller = require('../models/Seller');

// Generar tokens JWT
const generateTokens = (userId, role) => {
  const accessToken = jwt.sign(
    { userId, role },
    process.env.JWT_SECRET,
    { expiresIn: '1h' }
  );
  
  const refreshToken = jwt.sign(
    { userId },
    process.env.REFRESH_SECRET,
    { expiresIn: '7d' }
  );
  
  return { accessToken, refreshToken };
};

// Registro de administrador
exports.registerAdmin = async (req, res) => {
  try {
    const {
      businessName,
      businessType,
      ruc,
      email,
      password,
      phone,
      address,
      contactName
    } = req.body;

    // Verificar si el email ya existe
    const existingUser = await User.findOne({ email });
    if (existingUser) {
      return res.status(409).json({
        success: false,
        message: 'El email ya está registrado'
      });
    }

    // Crear negocio
    const business = new Business({
      name: businessName,
      type: businessType,
      ruc,
      phone,
      address,
      contactName,
      isVerified: false
    });
    await business.save();

    // Crear usuario administrador
    const user = new User({
      email,
      password,
      role: 'ADMIN',
      businessId: business._id,
      isVerified: false
    });
    await user.save();

    // Generar tokens
    const { accessToken, refreshToken } = generateTokens(user._id, 'ADMIN');

    res.status(201).json({
      success: true,
      message: 'Administrador registrado exitosamente',
      data: {
        adminId: user._id,
        businessId: business._id,
        email: user.email,
        businessName: business.name,
        verificationRequired: true,
        accessToken,
        refreshToken
      }
    });

  } catch (error) {
    console.error('Register admin error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

// Login
exports.login = async (req, res) => {
  try {
    const { email, password, deviceFingerprint, role } = req.body;

    // Buscar usuario
    const user = await User.findOne({ email, role }).populate('businessId');
    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Credenciales inválidas'
      });
    }

    // Verificar contraseña
    const isValidPassword = await user.comparePassword(password);
    if (!isValidPassword) {
      return res.status(401).json({
        success: false,
        message: 'Credenciales inválidas'
      });
    }

    // Verificar si el usuario está activo
    if (!user.isActive) {
      return res.status(403).json({
        success: false,
        message: 'Cuenta desactivada'
      });
    }

    // Actualizar último login y fingerprint
    user.lastLogin = new Date();
    if (deviceFingerprint && !user.deviceFingerprints.includes(deviceFingerprint)) {
      user.deviceFingerprints.push(deviceFingerprint);
    }
    await user.save();

    // Generar tokens
    const { accessToken, refreshToken } = generateTokens(user._id, user.role);

    // Guardar refresh token
    user.refreshTokens.push(refreshToken);
    await user.save();

    // Preparar datos de respuesta
    const userData = {
      id: user._id,
      email: user.email,
      role: user.role,
      isVerified: user.isVerified
    };

    if (user.role === 'ADMIN' && user.businessId) {
      userData.businessId = user.businessId._id;
      userData.businessName = user.businessId.name;
    }

    res.json({
      success: true,
      message: 'Login exitoso',
      data: {
        accessToken,
        refreshToken,
        expiresIn: 3600,
        user: userData
      }
    });

  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

// Refresh token
exports.refreshToken = async (req, res) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(401).json({
        success: false,
        message: 'Refresh token requerido'
      });
    }

    // Verificar refresh token
    const decoded = jwt.verify(refreshToken, process.env.REFRESH_SECRET);
    const user = await User.findById(decoded.userId);

    if (!user || !user.refreshTokens.includes(refreshToken)) {
      return res.status(401).json({
        success: false,
        message: 'Refresh token inválido'
      });
    }

    // Generar nuevos tokens
    const { accessToken, refreshToken: newRefreshToken } = generateTokens(user._id, user.role);

    // Actualizar refresh tokens
    user.refreshTokens = user.refreshTokens.filter(token => token !== refreshToken);
    user.refreshTokens.push(newRefreshToken);
    await user.save();

    res.json({
      success: true,
      data: {
        accessToken,
        refreshToken: newRefreshToken,
        expiresIn: 3600
      }
    });

  } catch (error) {
    console.error('Refresh token error:', error);
    res.status(401).json({
      success: false,
      message: 'Refresh token inválido'
    });
  }
};

// Logout
exports.logout = async (req, res) => {
  try {
    const { refreshToken } = req.body;
    const userId = req.user.userId;

    const user = await User.findById(userId);
    if (user) {
      user.refreshTokens = user.refreshTokens.filter(token => token !== refreshToken);
      await user.save();
    }

    res.json({
      success: true,
      message: 'Logout exitoso'
    });

  } catch (error) {
    console.error('Logout error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};
```

#### transactionController.js
```javascript
const Transaction = require('../models/Transaction');
const Business = require('../models/Business');
const Branch = require('../models/Branch');
const Seller = require('../models/Seller');

// Obtener transacciones con filtros
exports.getTransactions = async (req, res) => {
  try {
    const {
      page = 1,
      limit = 50,
      startDate,
      endDate,
      branchId,
      sellerId,
      status = 'all'
    } = req.query;

    const userId = req.user.userId;
    const userRole = req.user.role;

    // Construir filtros
    const filters = {};

    // Si es vendedor, solo puede ver sus transacciones
    if (userRole === 'SELLER') {
      const seller = await Seller.findOne({ userId });
      if (!seller) {
        return res.status(404).json({
          success: false,
          message: 'Vendedor no encontrado'
        });
      }
      filters.sellerId = seller._id;
    } else if (userRole === 'ADMIN') {
      // Si es admin, filtrar por su negocio
      const business = await Business.findOne({ userId });
      if (business) {
        filters.businessId = business._id;
      }
    }

    // Filtros adicionales
    if (branchId) filters.branchId = branchId;
    if (sellerId) filters.sellerId = sellerId;
    if (startDate || endDate) {
      filters.timestamp = {};
      if (startDate) filters.timestamp.$gte = new Date(startDate);
      if (endDate) filters.timestamp.$lte = new Date(endDate);
    }
    if (status !== 'all') {
      filters.isProcessed = status === 'confirmed';
    }

    // Paginación
    const skip = (parseInt(page) - 1) * parseInt(limit);

    // Ejecutar consulta
    const [transactions, total] = await Promise.all([
      Transaction.find(filters)
        .populate('businessId', 'name')
        .populate('branchId', 'name code')
        .populate('sellerId', 'name')
        .sort({ timestamp: -1 })
        .skip(skip)
        .limit(parseInt(limit)),
      Transaction.countDocuments(filters)
    ]);

    // Formatear respuesta
    const formattedTransactions = transactions.map(txn => ({
      id: txn._id,
      securityCode: txn.securityCode,
      amount: txn.amount.toFixed(2),
      timestamp: txn.timestamp,
      description: txn.description,
      type: txn.type,
      businessName: txn.businessId?.name,
      branchId: txn.branchId?._id,
      branchName: txn.branchId?.name,
      sellerId: txn.sellerId?._id,
      sellerName: txn.sellerId?.name,
      isProcessed: txn.isProcessed,
      paymentMethod: txn.paymentMethod,
      customerPhone: txn.customerPhone
    }));

    res.json({
      success: true,
      data: {
        transactions: formattedTransactions,
        pagination: {
          currentPage: parseInt(page),
          totalPages: Math.ceil(total / parseInt(limit)),
          totalItems: total,
          itemsPerPage: parseInt(limit)
        }
      }
    });

  } catch (error) {
    console.error('Get transactions error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

// Confirmar transacción
exports.confirmTransaction = async (req, res) => {
  try {
    const { transactionId } = req.params;
    const { sellerId, notes } = req.body;
    const userId = req.user.userId;

    // Verificar que la transacción existe
    const transaction = await Transaction.findById(transactionId)
      .populate('businessId')
      .populate('branchId');

    if (!transaction) {
      return res.status(404).json({
        success: false,
        message: 'Transacción no encontrada'
      });
    }

    // Verificar permisos
    if (req.user.role === 'ADMIN') {
      const business = await Business.findOne({ userId });
      if (!business || !transaction.businessId._id.equals(business._id)) {
        return res.status(403).json({
          success: false,
          message: 'No tienes permisos para esta transacción'
        });
      }
    } else if (req.user.role === 'SELLER') {
      const seller = await Seller.findOne({ userId });
      if (!seller || !transaction.sellerId.equals(seller._id)) {
        return res.status(403).json({
          success: false,
          message: 'No tienes permisos para esta transacción'
        });
      }
    }

    // Actualizar transacción
    transaction.isProcessed = true;
    transaction.processedAt = new Date();
    transaction.sellerId = sellerId || transaction.sellerId;
    transaction.notes = notes;
    await transaction.save();

    // Enviar notificación
    // TODO: Implementar servicio de notificaciones

    res.json({
      success: true,
      message: 'Transacción confirmada exitosamente',
      data: {
        transactionId: transaction._id,
        isProcessed: transaction.isProcessed,
        processedAt: transaction.processedAt
      }
    });

  } catch (error) {
    console.error('Confirm transaction error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};
```

### Middleware de Autenticación

#### auth.js
```javascript
const jwt = require('jsonwebtoken');
const User = require('../models/User');

const authMiddleware = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        success: false,
        message: 'Token de acceso requerido'
      });
    }

    const token = authHeader.substring(7); // Remove 'Bearer ' prefix

    // Verificar token
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    
    // Verificar que el usuario existe y está activo
    const user = await User.findById(decoded.userId);
    if (!user || !user.isActive) {
      return res.status(401).json({
        success: false,
        message: 'Usuario no válido o inactivo'
      });
    }

    // Agregar información del usuario al request
    req.user = {
      userId: user._id,
      role: user.role,
      businessId: user.businessId,
      sellerId: user.sellerId
    };

    next();

  } catch (error) {
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({
        success: false,
        message: 'Token inválido'
      });
    }
    
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: 'Token expirado'
      });
    }

    console.error('Auth middleware error:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
};

// Middleware para verificar rol específico
const requireRole = (roles) => {
  return (req, res, next) => {
    if (!roles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        message: 'Permisos insuficientes'
      });
    }
    next();
  };
};

module.exports = { authMiddleware, requireRole };
```

### Servicios

#### notificationService.js
```javascript
const admin = require('firebase-admin');

class NotificationService {
  constructor() {
    // Inicializar Firebase Admin SDK
    if (!admin.apps.length) {
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId: process.env.FIREBASE_PROJECT_ID,
          clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
          privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n')
        })
      });
    }
  }

  async sendNotification(targetType, targetId, title, message, data = {}) {
    try {
      let tokens = [];

      if (targetType === 'all') {
        // Obtener todos los tokens de usuarios activos
        const users = await User.find({ isActive: true }).select('fcmToken');
        tokens = users.map(user => user.fcmToken).filter(token => token);
      } else if (targetType === 'seller' && targetId) {
        // Obtener token del vendedor específico
        const seller = await Seller.findById(targetId).populate('userId');
        if (seller && seller.userId.fcmToken) {
          tokens = [seller.userId.fcmToken];
        }
      } else if (targetType === 'admin' && targetId) {
        // Obtener token del administrador específico
        const admin = await User.findById(targetId);
        if (admin && admin.fcmToken) {
          tokens = [admin.fcmToken];
        }
      }

      if (tokens.length === 0) {
        console.log('No tokens found for notification');
        return;
      }

      const messagePayload = {
        notification: {
          title,
          body: message
        },
        data: {
          ...data,
          timestamp: new Date().toISOString()
        },
        tokens
      };

      const response = await admin.messaging().sendMulticast(messagePayload);
      
      console.log(`Notification sent to ${response.successCount} devices`);
      
      return {
        success: true,
        sentCount: response.successCount,
        failedCount: response.failureCount
      };

    } catch (error) {
      console.error('Send notification error:', error);
      throw error;
    }
  }

  async saveNotification(userId, title, message, type, data = {}) {
    try {
      const notification = new Notification({
        userId,
        title,
        message,
        type,
        data,
        isRead: false
      });

      await notification.save();
      return notification;

    } catch (error) {
      console.error('Save notification error:', error);
      throw error;
    }
  }
}

module.exports = new NotificationService();
```

## Frontend - Kotlin Multiplatform

### Estructura del Proyecto
```
shared/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   ├── data/
│   │   │   │   ├── models/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   └── Business.kt
│   │   │   │   ├── repositories/
│   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   └── TransactionRepository.kt
│   │   │   │   └── network/
│   │   │   │       ├── ApiClient.kt
│   │   │   │       └── ApiService.kt
│   │   │   ├── domain/
│   │   │   │   ├── usecases/
│   │   │   │   │   ├── LoginUseCase.kt
│   │   │   │   │   └── GetTransactionsUseCase.kt
│   │   │   │   └── models/
│   │   │   └── presentation/
│   │   │       ├── viewmodels/
│   │   │       └── screens/
│   │   └── resources/
│   └── androidMain/
│       └── kotlin/
│           └── di/
│               └── AppModule.kt
```

### Modelos de Datos

#### User.kt
```kotlin
@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("role") val role: String,
    @SerialName("businessId") val businessId: String? = null,
    @SerialName("businessName") val businessName: String? = null,
    @SerialName("isVerified") val isVerified: Boolean = false
)

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("deviceFingerprint") val deviceFingerprint: String,
    @SerialName("role") val role: String
)

@Serializable
data class LoginResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: LoginData
)

@Serializable
data class LoginData(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("expiresIn") val expiresIn: Int,
    @SerialName("user") val user: User
)
```

#### Transaction.kt
```kotlin
@Serializable
data class Transaction(
    @SerialName("id") val id: String,
    @SerialName("securityCode") val securityCode: String,
    @SerialName("amount") val amount: String,
    @SerialName("timestamp") val timestamp: String,
    @SerialName("description") val description: String? = null,
    @SerialName("type") val type: String,
    @SerialName("businessName") val businessName: String? = null,
    @SerialName("branchId") val branchId: String? = null,
    @SerialName("branchName") val branchName: String? = null,
    @SerialName("sellerId") val sellerId: String? = null,
    @SerialName("sellerName") val sellerName: String? = null,
    @SerialName("isProcessed") val isProcessed: Boolean,
    @SerialName("paymentMethod") val paymentMethod: String? = null,
    @SerialName("customerPhone") val customerPhone: String? = null
)

@Serializable
data class TransactionsResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: TransactionsData
)

@Serializable
data class TransactionsData(
    @SerialName("transactions") val transactions: List<Transaction>,
    @SerialName("pagination") val pagination: Pagination
)

@Serializable
data class Pagination(
    @SerialName("currentPage") val currentPage: Int,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("totalItems") val totalItems: Int,
    @SerialName("itemsPerPage") val itemsPerPage: Int
)
```

### Servicios de Red

#### ApiService.kt
```kotlin
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): RefreshResponse
    
    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): ApiResponse
    
    @GET("transactions")
    suspend fun getTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("branchId") branchId: String? = null,
        @Query("sellerId") sellerId: String? = null,
        @Query("status") status: String = "all"
    ): TransactionsResponse
    
    @POST("transactions/{transactionId}/confirm")
    suspend fun confirmTransaction(
        @Path("transactionId") transactionId: String,
        @Body request: ConfirmTransactionRequest
    ): ApiResponse
    
    @GET("admin/dashboard")
    suspend fun getAdminDashboard(
        @Query("period") period: String = "today",
        @Query("branchId") branchId: String? = null
    ): DashboardResponse
    
    @GET("sellers/dashboard")
    suspend fun getSellerDashboard(
        @Query("period") period: String = "today"
    ): SellerDashboardResponse
}
```

#### ApiClient.kt
```kotlin
class ApiClient {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Auth) {
            bearer {
                loadTokens {
                    val token = TokenManager.getAccessToken()
                    if (token != null) {
                        BearerTokens(token, "")
                    } else {
                        null
                    }
                }
            }
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    private val baseUrl = "https://api.yapechamo.com/api/"
    
    val authService: AuthService = AuthService(httpClient, baseUrl)
    val transactionService: TransactionService = TransactionService(httpClient, baseUrl)
    val adminService: AdminService = AdminService(httpClient, baseUrl)
    val sellerService: SellerService = SellerService(httpClient, baseUrl)
    
    fun close() {
        httpClient.close()
    }
}
```

### Repositorios

#### AuthRepository.kt
```kotlin
class AuthRepository(private val apiService: ApiService) {
    
    suspend fun login(email: String, password: String, role: String): Result<LoginData> {
        return try {
            val deviceFingerprint = getDeviceFingerprint()
            val request = LoginRequest(email, password, deviceFingerprint, role)
            val response = apiService.login(request)
            
            if (response.success) {
                // Guardar tokens
                TokenManager.saveTokens(
                    response.data.accessToken,
                    response.data.refreshToken
                )
                
                // Guardar información del usuario
                UserManager.saveUser(response.data.user)
                
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken == null) {
                return Result.failure(Exception("No refresh token available"))
            }
            
            val request = RefreshRequest(refreshToken)
            val response = apiService.refreshToken(request)
            
            if (response.success) {
                TokenManager.saveTokens(
                    response.data.accessToken,
                    response.data.refreshToken
                )
                Result.success(response.data.accessToken)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken != null) {
                val request = LogoutRequest(refreshToken)
                apiService.logout(request)
            }
            
            // Limpiar tokens y datos del usuario
            TokenManager.clearTokens()
            UserManager.clearUser()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Limpiar tokens incluso si falla la llamada al servidor
            TokenManager.clearTokens()
            UserManager.clearUser()
            Result.success(Unit)
        }
    }
    
    private fun getDeviceFingerprint(): String {
        // Implementar generación de fingerprint único del dispositivo
        return "device_${System.currentTimeMillis()}_${UUID.randomUUID()}"
    }
}
```

#### TransactionRepository.kt
```kotlin
class TransactionRepository(private val apiService: ApiService) {
    
    suspend fun getTransactions(
        page: Int = 1,
        limit: Int = 50,
        startDate: String? = null,
        endDate: String? = null,
        branchId: String? = null,
        sellerId: String? = null,
        status: String = "all"
    ): Result<TransactionsData> {
        return try {
            val response = apiService.getTransactions(
                page, limit, startDate, endDate, branchId, sellerId, status
            )
            
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Error al obtener transacciones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun confirmTransaction(
        transactionId: String,
        sellerId: String? = null,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val request = ConfirmTransactionRequest(sellerId, notes)
            val response = apiService.confirmTransaction(transactionId, request)
            
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAdminDashboard(
        period: String = "today",
        branchId: String? = null
    ): Result<DashboardData> {
        return try {
            val response = apiService.getAdminDashboard(period, branchId)
            
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Error al obtener dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSellerDashboard(period: String = "today"): Result<SellerDashboardData> {
        return try {
            val response = apiService.getSellerDashboard(period)
            
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Error al obtener dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Casos de Uso

#### LoginUseCase.kt
```kotlin
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        role: String
    ): Result<LoginData> {
        // Validaciones
        if (email.isBlank()) {
            return Result.failure(Exception("Email es requerido"))
        }
        
        if (password.isBlank()) {
            return Result.failure(Exception("Contraseña es requerida"))
        }
        
        if (role.isBlank()) {
            return Result.failure(Exception("Rol es requerido"))
        }
        
        // Validar formato de email
        if (!isValidEmail(email)) {
            return Result.failure(Exception("Formato de email inválido"))
        }
        
        // Validar longitud de contraseña
        if (password.length < 8) {
            return Result.failure(Exception("La contraseña debe tener al menos 8 caracteres"))
        }
        
        return authRepository.login(email, password, role)
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
}
```

### ViewModels

#### AuthViewModel.kt
```kotlin
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            loginUseCase(email, password, role)
                .onSuccess { loginData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = loginData.user
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _uiState.value = AuthUiState()
                }
                .onFailure { error ->
                    // Logout local incluso si falla el servidor
                    _uiState.value = AuthUiState()
                }
        }
    }
    
    fun refreshToken() {
        viewModelScope.launch {
            refreshTokenUseCase()
                .onFailure { error ->
                    // Si falla el refresh, hacer logout
                    logout()
                }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
```

## Configuración de Base de Datos

### MongoDB - Esquemas de Índices
```javascript
// Índices para optimizar consultas
db.users.createIndex({ "email": 1 }, { unique: true })
db.users.createIndex({ "role": 1 })
db.users.createIndex({ "businessId": 1 })

db.transactions.createIndex({ "securityCode": 1 }, { unique: true })
db.transactions.createIndex({ "businessId": 1, "timestamp": -1 })
db.transactions.createIndex({ "sellerId": 1, "timestamp": -1 })
db.transactions.createIndex({ "isProcessed": 1 })

db.businesses.createIndex({ "ruc": 1 }, { unique: true })
db.businesses.createIndex({ "email": 1 })

db.sellers.createIndex({ "email": 1 }, { unique: true })
db.sellers.createIndex({ "businessId": 1 })
db.sellers.createIndex({ "branchId": 1 })
```

### Variables de Entorno (.env)
```bash
# Base de datos
MONGODB_URI=mongodb://localhost:27017/yapechamo
MONGODB_DB_NAME=yapechamo

# JWT
JWT_SECRET=your_super_secret_jwt_key_here
REFRESH_SECRET=your_super_secret_refresh_key_here

# Firebase (para notificaciones push)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYour private key here\n-----END PRIVATE KEY-----\n"

# Servidor
PORT=3000
NODE_ENV=development

# Rate limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=1000

# CORS
CORS_ORIGIN=http://localhost:3000,https://yourdomain.com
```

## Testing

### Tests de API con Jest
```javascript
// tests/auth.test.js
const request = require('supertest');
const app = require('../app');
const User = require('../models/User');

describe('Auth API', () => {
  beforeEach(async () => {
    await User.deleteMany({});
  });

  describe('POST /api/auth/login', () => {
    it('should login successfully with valid credentials', async () => {
      // Crear usuario de prueba
      const user = new User({
        email: 'test@example.com',
        password: 'password123',
        role: 'ADMIN'
      });
      await user.save();

      const response = await request(app)
        .post('/api/auth/login')
        .send({
          email: 'test@example.com',
          password: 'password123',
          deviceFingerprint: 'test_device',
          role: 'ADMIN'
        });

      expect(response.status).toBe(200);
      expect(response.body.success).toBe(true);
      expect(response.body.data.accessToken).toBeDefined();
      expect(response.body.data.user.email).toBe('test@example.com');
    });

    it('should fail with invalid credentials', async () => {
      const response = await request(app)
        .post('/api/auth/login')
        .send({
          email: 'test@example.com',
          password: 'wrongpassword',
          deviceFingerprint: 'test_device',
          role: 'ADMIN'
        });

      expect(response.status).toBe(401);
      expect(response.body.success).toBe(false);
    });
  });
});
```

Esta documentación proporciona una base sólida para implementar todas las APIs necesarias para el sistema YapeChamo. ¿Te gustaría que profundice en alguna sección específica o que agregue más detalles sobre algún aspecto en particular?
