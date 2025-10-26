package org.sysarp.project.ui.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Ejemplos de uso del sistema de loading
 * 
 * IMPORTANTE: Este archivo es solo para documentación y ejemplos.
 * No debe ser usado en producción.
 */

/**
 * Ejemplo 1: Loading básico con overlay
 */
@Composable
fun LoadingExample1() {
    val loadingState = rememberLoadingState()
    val coroutineScope = rememberCoroutineScope()
    
    LoadingHandler(loadingState = loadingState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launchWithLoading(
                        loadingState = loadingState,
                        message = LoadingMessages.LOADING,
                        minDuration = 2000L
                    ) {
                        // Simular operación lenta
                        delay(3000)
                    }
                }
            ) {
                Text("Cargar datos")
            }
        }
    }
}

/**
 * Ejemplo 2: Loading con diferentes mensajes
 */
@Composable
fun LoadingExample2() {
    val loadingState = rememberLoadingState()
    val coroutineScope = rememberCoroutineScope()
    
    LoadingHandler(loadingState = loadingState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launchWithLoading(
                        loadingState = loadingState,
                        message = LoadingMessages.SAVING,
                        minDuration = 1000L
                    ) {
                        // Simular guardado con pasos
                        loadingState.updateMessage("Validando datos...")
                        delay(1000)
                        
                        loadingState.updateMessage("Guardando en servidor...")
                        delay(1500)
                        
                        loadingState.updateMessage("Finalizando...")
                        delay(500)
                    }
                }
            ) {
                Text("Guardar con pasos")
            }
        }
    }
}

/**
 * Ejemplo 3: Loading inline para componentes pequeños
 */
@Composable
fun LoadingExample3() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Datos del usuario:")
        
        // Loading pequeño
        InlineLoading(
            message = "Cargando perfil...",
            size = LoadingSize.Small
        )
        
        Text("Estadísticas:")
        
        // Loading mediano
        InlineLoading(
            message = "Calculando estadísticas...",
            size = LoadingSize.Medium
        )
        
        Text("Reportes:")
        
        // Loading grande
        InlineLoading(
            message = "Generando reportes...",
            size = LoadingSize.Large
        )
    }
}

/**
 * Ejemplo 4: Loading en servicios/repositorios
 */
class ExampleService {
    
    /**
     * Ejemplo de cómo usar loading en un servicio
     */
    suspend fun saveData(
        loadingState: LoadingState,
        data: String
    ): Result<String> {
        return loadingState.withLoading(
            message = LoadingMessages.SAVING,
            minDuration = 1000L
        ) {
            // Simular validación
            loadingState.updateMessage("Validando datos...")
            delay(500)
            
            // Simular envío a API
            loadingState.updateMessage("Enviando al servidor...")
            delay(1000)
            
            // Simular procesamiento
            loadingState.updateMessage("Procesando...")
            delay(500)
            
            // Retornar resultado
            Result.success("Datos guardados correctamente")
        }
    }
    
    /**
     * Ejemplo de operación que puede fallar
     */
    suspend fun riskyOperation(
        loadingState: LoadingState
    ): Result<String> {
        return try {
            loadingState.withLoading(
                message = LoadingMessages.PROCESSING,
                minDuration = 1000L
            ) {
                delay(2000)
                
                // Simular error aleatorio
                if (Math.random() > 0.5) {
                    throw Exception("Error simulado")
                }
                
                Result.success("Operación exitosa")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Ejemplo 5: Uso en ViewModels o casos de uso
 */
@Composable
fun LoadingExample5() {
    val loadingState = rememberLoadingState()
    val coroutineScope = rememberCoroutineScope()
    val service = ExampleService()
    
    LoadingHandler(loadingState = loadingState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launchWithLoading(
                        loadingState = loadingState,
                        message = LoadingMessages.PROCESSING,
                        onError = { error ->
                            // Manejar error
                            println("Error: ${error.message}")
                        }
                    ) {
                        val result = service.saveData(loadingState, "datos de ejemplo")
                        result.fold(
                            onSuccess = { message ->
                                println("Éxito: $message")
                            },
                            onFailure = { error ->
                                throw error
                            }
                        )
                    }
                }
            ) {
                Text("Operación con servicio")
            }
        }
    }
}

/**
 * GUÍA DE USO:
 * 
 * 1. LOADING GLOBAL (Overlay):
 *    - Usa LoadingHandler + rememberLoadingState()
 *    - Para operaciones que bloquean toda la pantalla
 *    - Ejemplo: Login, registro, operaciones críticas
 * 
 * 2. LOADING INLINE:
 *    - Usa InlineLoading()
 *    - Para secciones específicas de la UI
 *    - Ejemplo: Cargar lista de elementos, estadísticas
 * 
 * 3. LOADING EN BOTONES:
 *    - Usa ButtonLoading()
 *    - Para mostrar estado de carga en botones
 *    - Ejemplo: Botones de envío, guardado
 * 
 * 4. LOADING MINIMAL:
 *    - Usa MinimalLoading()
 *    - Para espacios muy pequeños
 *    - Ejemplo: Iconos de carga, indicadores pequeños
 * 
 * 5. MENSAJES PREDEFINIDOS:
 *    - Usa LoadingMessages.* para consistencia
 *    - Ejemplo: SAVING, LOADING, PROCESSING, etc.
 * 
 * 6. DURACIÓN MÍNIMA:
 *    - Usa minDuration para evitar flicker
 *    - Recomendado: 500-1000ms para operaciones rápidas
 * 
 * 7. ACTUALIZAR MENSAJES:
 *    - Usa loadingState.updateMessage() para mostrar progreso
 *    - Ejemplo: "Validando..." -> "Guardando..." -> "Finalizando..."
 */