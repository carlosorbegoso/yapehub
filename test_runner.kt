import org.sysarp.project.test.*

fun main() {
    println("🧪 [DUPLICATE TEST] Ejecutando prueba de detección de duplicados...")
    println("=" * 60)
    
    val duplicateTest = DuplicateDetectionTest()
    val result = duplicateTest.runTest()
    
    println("\n" + "=" * 60)
    if (result.success) {
        println("🎉 ¡PRUEBA EXITOSA! La lógica de duplicados funciona correctamente")
    } else {
        println("❌ ¡PRUEBA FALLIDA! Hay problemas con la lógica de duplicados")
    }
    println("=" * 60)
}
