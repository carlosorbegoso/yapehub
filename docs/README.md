# 📚 Documentación YapeHub

Bienvenido a la documentación completa de YapeHub. Aquí encontrarás todas las guías necesarias para desarrollar, compilar y distribuir la aplicación.

## 📋 Índice de Contenidos

### 🚀 Guías de Inicio Rápido
- [**⚡ Referencia Rápida**](./guides/QUICK_REFERENCE.md) - Comandos más usados
- [**Guía de Ejecución de Scripts**](./guides/SCRIPT_EXECUTION_GUIDE.md) - Cómo usar todos los scripts del proyecto
- [**Guía de Firma de APK**](./guides/SIGNING_GUIDE.md) - Proceso completo para firmar APKs
- [**Configuración del Proyecto**](./guides/PROJECT_SETUP.md) - Setup inicial y configuración

### 🔧 Desarrollo
- [**Plan de Refactorización**](./guides/REFACTORING_PLAN.md) - Mejoras de código planificadas
- [**Arquitectura del Proyecto**](./guides/ARCHITECTURE.md) - Estructura y patrones utilizados
- [**Guía de Contribución**](./guides/CONTRIBUTING.md) - Cómo contribuir al proyecto

### 📱 Distribución
- [**Éxito de Build**](./guides/BUILD_SUCCESS.md) - Confirmación de builds exitosos
- [**Guía de Play Store**](./guides/PLAY_STORE_GUIDE.md) - Publicación en Google Play
- [**Testing y QA**](./guides/TESTING_GUIDE.md) - Pruebas y control de calidad

### 🛠️ Scripts Disponibles
- [**Scripts de Build**](./scripts/) - Todos los scripts automatizados
- [**Lista de Seguridad**](./guides/SECURITY_CHECKLIST.md) - Archivos sensibles y seguridad
- [**Herramientas de Desarrollo**](./guides/DEV_TOOLS.md) - Herramientas útiles

## 🎯 Inicio Rápido

### Para Desarrolladores Nuevos:
1. Lee la [Guía de Configuración del Proyecto](./guides/PROJECT_SETUP.md)
2. Ejecuta: `./scripts/setup-project.sh`
3. Genera keystore: `./scripts/generate-keystore.sh`
4. Build completo: `./scripts/build-all.sh`

### Para Builds de Producción:
1. **Build completo**: `./scripts/build-all.sh` (APK + AAB)
2. **Solo APK**: `./scripts/build-all.sh -t apk`
3. **Solo AAB**: `./scripts/build-all.sh -t aab`

### Para Desarrollo Diario:
1. **Build rápido**: `./scripts/dev-build.sh`
2. **Consulta rápida**: [Referencia Rápida](./guides/QUICK_REFERENCE.md)

### Para Distribución:
1. Consulta la [Guía de Play Store](./guides/PLAY_STORE_GUIDE.md)
2. Usa el archivo `.aab` generado
3. Sigue el proceso de publicación

## 📞 Soporte

Si tienes problemas:
1. Revisa la documentación relevante
2. Ejecuta `./scripts/troubleshoot.sh`
3. Consulta los logs en `.kotlin/errors/`
4. Crea un issue en el repositorio

---

**YapeHub** - Documentación actualizada el $(date +"%d/%m/%Y")