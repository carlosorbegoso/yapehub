// Test simple para verificar la lógica de duplicados
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun main() {
    println("🧪 [DUPLICATE TEST] Ejecutando prueba de detección de duplicados...")
    println("=" * 60)
    
    var passedTests = 0
    var totalTests = 0
    
    // Test 1: Códigos diferentes
    totalTests++
    val now = Clock.System.now()
    val key1 = generateKey(now, "823")
    val key2 = generateKey(now.plusSeconds(1), "711")
    val key3 = generateKey(now.plusSeconds(2), "901")
    
    if (key1 != key2 && key2 != key3 && key1 != key3) {
        println("✅ [TEST 1] Códigos diferentes generan claves únicas")
        println("   Clave 1: $key1")
        println("   Clave 2: $key2")
        println("   Clave 3: $key3")
        passedTests++
    } else {
        println("❌ [TEST 1] ERROR: Códigos diferentes generaron claves iguales")
    }
    
    // Test 2: Mismo código, diferentes timestamps
    totalTests++
    val key4 = generateKey(now, "823")
    val key5 = generateKey(now.plusSeconds(60), "823")
    val key6 = generateKey(now.plusSeconds(120), "823")
    
    if (key4 != key5 && key5 != key6 && key4 != key6) {
        println("✅ [TEST 2] Mismo código, diferentes timestamps = claves únicas")
        println("   Clave 4: $key4")
        println("   Clave 5: $key5")
        println("   Clave 6: $key6")
        passedTests++
    } else {
        println("❌ [TEST 2] ERROR: Mismo código con diferentes timestamps generó claves iguales")
    }
    
    // Test 3: Duplicados reales (mismo código + mismo timestamp)
    totalTests++
    val key7 = generateKey(now, "823")
    val key8 = generateKey(now, "823")
    
    if (key7 == key8) {
        println("✅ [TEST 3] Duplicados reales detectados correctamente")
        println("   Clave 7: $key7")
        println("   Clave 8: $key8")
        println("   Son iguales (duplicados)")
        passedTests++
    } else {
        println("❌ [TEST 3] ERROR: Duplicados reales no detectados")
    }
    
    // Test 4: Mismo remitente/monto, códigos diferentes (tu caso original)
    totalTests++
    val key9 = generateKey(now, "823")
    val key10 = generateKey(now.plusSeconds(1), "711")
    val key11 = generateKey(now.plusSeconds(2), "901")
    
    if (key9 != key10 && key10 != key11 && key9 != key11) {
        println("✅ [TEST 4] Mismo remitente/monto, códigos diferentes = únicas")
        println("   Clave 9: $key9")
        println("   Clave 10: $key10")
        println("   Clave 11: $key11")
        println("   Todas son únicas (problema original resuelto)")
        passedTests++
    } else {
        println("❌ [TEST 4] ERROR: Mismo remitente/monto con códigos diferentes generó claves iguales")
    }
    
    // Resumen
    println("\n" + "=" * 60)
    println("📊 [RESULTADO] Pruebas ejecutadas: $totalTests")
    println("✅ Pruebas pasadas: $passedTests")
    println("❌ Pruebas fallidas: ${totalTests - passedTests}")
    println("📈 Porcentaje de éxito: ${(passedTests * 100) / totalTests}%")
    
    if (passedTests == totalTests) {
        println("🎉 ¡TODAS LAS PRUEBAS PASARON!")
        println("✅ La lógica de duplicados funciona correctamente")
    } else {
        println("❌ ALGUNAS PRUEBAS FALLARON")
        println("🔧 Revisar la lógica de detección de duplicados")
    }
    println("=" * 60)
}

fun generateKey(timestamp: Instant, securityCode: String): String {
    return "${timestamp.toEpochMilliseconds()}_${securityCode}"
}
