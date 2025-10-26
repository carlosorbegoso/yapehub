# 📚 Documentación YapeHub

Bienvenido a la documentación completa de YapeHub. Aquí encontrarás todas las guías necesarias para desarrollar, compilar y distribuir la aplicación.

## 📋 Índice de Contenidos

### 🚀 Guías de Inicio Rápido
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
2. Ejecuta los scripts de setup: `./scripts/setup-project.sh`
3. Compila tu primera APK: `./scripts/build-release.sh`

### Para Builds de Producción:
1. Revisa la [Guía de Firma de APK](./guides/SIGNING_GUIDE.md)
2. Ejecuta: `./scripts/build-release.sh` o `./scripts/build-bundle.sh`
3. Verifica con: `./scripts/verify-apk.sh`

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