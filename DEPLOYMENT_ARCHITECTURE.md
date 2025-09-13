# Arquitectura y Despliegue - Sistema YapeChamo

## Arquitectura General

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │   Web Admin     │    │   Web Seller    │
│   (Android/iOS) │    │   Dashboard     │    │   Dashboard     │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      API Gateway          │
                    │   (Rate Limiting, Auth)   │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │     Backend Services       │
                    │  (Node.js + Express)       │
                    └─────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────┴───────┐    ┌─────────┴───────┐    ┌─────────┴───────┐
│   MongoDB       │    │   Redis Cache   │    │   Firebase      │
│   (Database)    │    │   (Sessions)    │    │   (Push Notif)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Stack Tecnológico

### Backend
- **Runtime**: Node.js 18+
- **Framework**: Express.js
- **Base de Datos**: MongoDB 6.0+
- **Cache**: Redis 7.0+
- **Autenticación**: JWT + Refresh Tokens
- **Notificaciones**: Firebase Cloud Messaging
- **Validación**: Joi
- **Testing**: Jest + Supertest
- **Documentación**: Swagger/OpenAPI

### Frontend (Mobile)
- **Framework**: Kotlin Multiplatform Mobile
- **UI**: Jetpack Compose (Android)
- **Networking**: Ktor Client
- **Serialización**: Kotlinx Serialization
- **Base de Datos Local**: SQLDelight
- **Dependency Injection**: Koin

### Infraestructura
- **Servidor**: AWS EC2 / Google Cloud / DigitalOcean
- **Base de Datos**: MongoDB Atlas
- **CDN**: CloudFlare
- **SSL**: Let's Encrypt
- **Monitoreo**: PM2 + Winston
- **CI/CD**: GitHub Actions

## Configuración del Servidor

### 1. Preparación del Servidor (Ubuntu 22.04)

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Instalar PM2 globalmente
sudo npm install -g pm2

# Instalar Nginx
sudo apt install nginx -y

# Instalar Certbot para SSL
sudo apt install certbot python3-certbot-nginx -y

# Instalar MongoDB (opcional si no usas Atlas)
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sudo systemctl start mongod
sudo systemctl enable mongod
```

### 2. Configuración de Nginx

```nginx
# /etc/nginx/sites-available/yapechamo
server {
    listen 80;
    server_name api.yapechamo.com;
    
    # Redirigir a HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yapechamo.com;
    
    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/api.yapechamo.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yapechamo.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Security Headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req zone=api burst=20 nodelay;
    
    # Proxy to Node.js app
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Static files caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json;
}
```

### 3. Configuración de PM2

```javascript
// ecosystem.config.js
module.exports = {
  apps: [{
    name: 'yapechamo-api',
    script: './src/app.js',
    instances: 'max',
    exec_mode: 'cluster',
    env: {
      NODE_ENV: 'development',
      PORT: 3000
    },
    env_production: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    error_file: './logs/err.log',
    out_file: './logs/out.log',
    log_file: './logs/combined.log',
    time: true,
    max_memory_restart: '1G',
    node_args: '--max-old-space-size=1024'
  }]
};
```

### 4. Scripts de Despliegue

```bash
#!/bin/bash
# deploy.sh

echo "🚀 Iniciando despliegue de YapeChamo API..."

# Variables
APP_NAME="yapechamo-api"
REPO_URL="https://github.com/tu-usuario/yapechamo-backend.git"
DEPLOY_DIR="/var/www/yapechamo"
BACKUP_DIR="/var/backups/yapechamo"

# Crear directorio de despliegue si no existe
sudo mkdir -p $DEPLOY_DIR
sudo mkdir -p $BACKUP_DIR

# Backup de la versión actual
if [ -d "$DEPLOY_DIR" ]; then
    echo "📦 Creando backup de la versión actual..."
    sudo cp -r $DEPLOY_DIR $BACKUP_DIR/backup-$(date +%Y%m%d-%H%M%S)
fi

# Clonar/actualizar repositorio
echo "📥 Descargando código fuente..."
if [ -d "$DEPLOY_DIR/.git" ]; then
    cd $DEPLOY_DIR
    sudo git pull origin main
else
    sudo git clone $REPO_URL $DEPLOY_DIR
fi

# Instalar dependencias
echo "📦 Instalando dependencias..."
cd $DEPLOY_DIR
sudo npm ci --production

# Ejecutar migraciones de base de datos
echo "🗄️ Ejecutando migraciones..."
sudo npm run migrate

# Reiniciar aplicación con PM2
echo "🔄 Reiniciando aplicación..."
sudo pm2 reload ecosystem.config.js --env production

# Verificar estado
echo "✅ Verificando estado de la aplicación..."
sudo pm2 status

echo "🎉 Despliegue completado!"
```

## Configuración de Base de Datos

### MongoDB Atlas (Recomendado)

```javascript
// Configuración de conexión
const mongoose = require('mongoose');

const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
      maxPoolSize: 10,
      serverSelectionTimeoutMS: 5000,
      socketTimeoutMS: 45000,
    });

    console.log(`MongoDB Connected: ${conn.connection.host}`);
  } catch (error) {
    console.error('Database connection error:', error);
    process.exit(1);
  }
};

module.exports = connectDB;
```

### Índices de Rendimiento

```javascript
// scripts/createIndexes.js
const mongoose = require('mongoose');

const createIndexes = async () => {
  try {
    // Índices para usuarios
    await mongoose.connection.db.collection('users').createIndex({ email: 1 }, { unique: true });
    await mongoose.connection.db.collection('users').createIndex({ role: 1 });
    await mongoose.connection.db.collection('users').createIndex({ businessId: 1 });
    
    // Índices para transacciones
    await mongoose.connection.db.collection('transactions').createIndex({ securityCode: 1 }, { unique: true });
    await mongoose.connection.db.collection('transactions').createIndex({ businessId: 1, timestamp: -1 });
    await mongoose.connection.db.collection('transactions').createIndex({ sellerId: 1, timestamp: -1 });
    await mongoose.connection.db.collection('transactions').createIndex({ isProcessed: 1 });
    await mongoose.connection.db.collection('transactions').createIndex({ timestamp: -1 });
    
    // Índices para negocios
    await mongoose.connection.db.collection('businesses').createIndex({ ruc: 1 }, { unique: true });
    await mongoose.connection.db.collection('businesses').createIndex({ email: 1 });
    
    // Índices para vendedores
    await mongoose.connection.db.collection('sellers').createIndex({ email: 1 }, { unique: true });
    await mongoose.connection.db.collection('sellers').createIndex({ businessId: 1 });
    await mongoose.connection.db.collection('sellers').createIndex({ branchId: 1 });
    
    console.log('✅ Índices creados exitosamente');
  } catch (error) {
    console.error('❌ Error creando índices:', error);
  }
};

module.exports = createIndexes;
```

## Configuración de Seguridad

### 1. Variables de Entorno Seguras

```bash
# .env.production
NODE_ENV=production
PORT=3000

# Base de datos
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/yapechamo?retryWrites=true&w=majority

# JWT Secrets (generar con: openssl rand -base64 64)
JWT_SECRET=your_super_secret_jwt_key_here_minimum_64_characters
REFRESH_SECRET=your_super_secret_refresh_key_here_minimum_64_characters

# Firebase
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxx@your-project.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nYour private key here\n-----END PRIVATE KEY-----\n"

# Redis (opcional)
REDIS_URL=redis://localhost:6379

# Rate Limiting
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=1000

# CORS
CORS_ORIGIN=https://yourdomain.com,https://admin.yourdomain.com

# Email (opcional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password

# Logging
LOG_LEVEL=info
LOG_FILE=./logs/app.log
```

### 2. Middleware de Seguridad

```javascript
// middleware/security.js
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const mongoSanitize = require('express-mongo-sanitize');
const xss = require('xss-clean');

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 100, // máximo 100 requests por IP
  message: {
    success: false,
    message: 'Demasiadas solicitudes desde esta IP, intenta de nuevo más tarde.'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

// Rate limiting específico para login
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 5, // máximo 5 intentos de login
  message: {
    success: false,
    message: 'Demasiados intentos de login, intenta de nuevo en 15 minutos.'
  },
  skipSuccessfulRequests: true,
});

const securityMiddleware = (app) => {
  // Helmet para headers de seguridad
  app.use(helmet({
    contentSecurityPolicy: {
      directives: {
        defaultSrc: ["'self'"],
        styleSrc: ["'self'", "'unsafe-inline'"],
        scriptSrc: ["'self'"],
        imgSrc: ["'self'", "data:", "https:"],
      },
    },
  }));

  // Rate limiting general
  app.use('/api/', limiter);
  
  // Rate limiting para login
  app.use('/api/auth/login', loginLimiter);
  
  // Sanitización de datos
  app.use(mongoSanitize());
  app.use(xss());
  
  // Validación de tamaño de request
  app.use(express.json({ limit: '10mb' }));
  app.use(express.urlencoded({ extended: true, limit: '10mb' }));
};

module.exports = securityMiddleware;
```

## Monitoreo y Logging

### 1. Configuración de Winston

```javascript
// utils/logger.js
const winston = require('winston');
const path = require('path');

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'yapechamo-api' },
  transports: [
    new winston.transports.File({ 
      filename: path.join('logs', 'error.log'), 
      level: 'error' 
    }),
    new winston.transports.File({ 
      filename: path.join('logs', 'combined.log') 
    }),
  ],
});

// En desarrollo, también log a consola
if (process.env.NODE_ENV !== 'production') {
  logger.add(new winston.transports.Console({
    format: winston.format.combine(
      winston.format.colorize(),
      winston.format.simple()
    )
  }));
}

module.exports = logger;
```

### 2. Health Check Endpoint

```javascript
// routes/health.js
const express = require('express');
const mongoose = require('mongoose');
const router = express.Router();

router.get('/health', async (req, res) => {
  try {
    const healthCheck = {
      uptime: process.uptime(),
      message: 'OK',
      timestamp: new Date().toISOString(),
      environment: process.env.NODE_ENV,
      version: process.env.npm_package_version || '1.0.0'
    };

    // Verificar conexión a MongoDB
    if (mongoose.connection.readyState === 1) {
      healthCheck.database = 'Connected';
    } else {
      healthCheck.database = 'Disconnected';
      return res.status(503).json(healthCheck);
    }

    res.status(200).json(healthCheck);
  } catch (error) {
    res.status(503).json({
      message: 'Service Unavailable',
      error: error.message
    });
  }
});

module.exports = router;
```

## CI/CD con GitHub Actions

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
    
    - name: Install dependencies
      run: npm ci
    
    - name: Run tests
      run: npm test
      env:
        NODE_ENV: test
        MONGODB_URI: mongodb://localhost:27017/yapechamo-test
    
    - name: Run linting
      run: npm run lint

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Deploy to server
      uses: appleboy/ssh-action@v0.1.5
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        key: ${{ secrets.SSH_KEY }}
        script: |
          cd /var/www/yapechamo
          git pull origin main
          npm ci --production
          npm run migrate
          pm2 reload ecosystem.config.js --env production
```

## Backup y Recuperación

### Script de Backup Automático

```bash
#!/bin/bash
# backup.sh

# Configuración
BACKUP_DIR="/var/backups/yapechamo"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="yapechamo"

# Crear directorio de backup
mkdir -p $BACKUP_DIR

# Backup de MongoDB
echo "🗄️ Creando backup de MongoDB..."
mongodump --db $DB_NAME --out $BACKUP_DIR/mongodb_$DATE

# Backup de archivos de aplicación
echo "📁 Creando backup de archivos..."
tar -czf $BACKUP_DIR/app_$DATE.tar.gz /var/www/yapechamo

# Backup de logs
echo "📝 Creando backup de logs..."
tar -czf $BACKUP_DIR/logs_$DATE.tar.gz /var/www/yapechamo/logs

# Limpiar backups antiguos (mantener últimos 7 días)
echo "🧹 Limpiando backups antiguos..."
find $BACKUP_DIR -name "mongodb_*" -mtime +7 -delete
find $BACKUP_DIR -name "app_*" -mtime +7 -delete
find $BACKUP_DIR -name "logs_*" -mtime +7 -delete

echo "✅ Backup completado: $DATE"
```

### Cron Job para Backup Automático

```bash
# Agregar al crontab (crontab -e)
# Backup diario a las 2:00 AM
0 2 * * * /var/www/yapechamo/scripts/backup.sh >> /var/log/backup.log 2>&1
```

## Escalabilidad

### 1. Load Balancer con Nginx

```nginx
# /etc/nginx/nginx.conf
upstream yapechamo_backend {
    least_conn;
    server 127.0.0.1:3000 max_fails=3 fail_timeout=30s;
    server 127.0.0.1:3001 max_fails=3 fail_timeout=30s;
    server 127.0.0.1:3002 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.yapechamo.com;
    
    location / {
        proxy_pass http://yapechamo_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 2. Configuración de PM2 para Múltiples Instancias

```javascript
// ecosystem.config.js
module.exports = {
  apps: [{
    name: 'yapechamo-api',
    script: './src/app.js',
    instances: 4, // 4 instancias
    exec_mode: 'cluster',
    env_production: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    increment_var: 'PORT',
    max_memory_restart: '1G',
    node_args: '--max-old-space-size=1024'
  }]
};
```

Esta documentación proporciona una guía completa para implementar, desplegar y mantener el sistema YapeChamo en producción. ¿Te gustaría que profundice en alguna sección específica o que agregue más detalles sobre algún aspecto en particular?
