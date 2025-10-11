package org.sysarp.project.config

object ServerConfig {
    // Configuración del servidor backend
    // Cambia esta URL por la IP de tu servidor
    
    // Para desarrollo local con EMULADOR ANDROID:
    //const val BASE_URL = "https://ks9ql0l7-8080.brs.devtunnels.ms/api"  // Dev Tunnel de VS Code
    
    // Opciones comunes:
    // const val BASE_URL = "http://10.0.2.2:8080/api"       // IP del host desde emulador Android
     const val BASE_URL = "http://192.168.1.100:8080/api"  // IP local de tu computadora
    // const val BASE_URL = "http://tu-servidor.com:8080/api" // Dominio real
    // const val BASE_URL = "https://tu-servidor.com/api"     // HTTPS (recomendado para producción)
    
    // Configuraciones adicionales
    const val TIMEOUT_SECONDS = 15L
    const val MAX_RETRIES = 3
}
