# 🚀 Guía de Configuración del Proyecto YapeHub

Esta guía te ayudará a configurar el proyecto YapeHub desde cero en tu máquina de desarrollo.

## 📋 Requisitos Previos

### Sistema Operativo
- ✅ macOS 10.15+
- ✅ Windows 10+
- ✅ Linux Ubuntu 18.04+

### Software Requerido
- **Java 17+** - [Descargar Adoptium](https://adoptium.net/)
- **Android Studio** - [Descargar](https://developer.android.com/studio)
- **Git** - [Descargar](https://git-scm.com/)

### Software Opcional
- **Android SDK Command Line Tools**
- **Dispositivo Android** o **Emulador**

## 🔧 Instalación Paso a Paso

### 1. **Clonar el Repositorio**
```bash
git clone https://github.com/tu-usuario/yapehub.git
cd yapehub
```

### 2. **Ejecutar Setup Automático**
```bash
# Hacer ejecutable el script
chmod +x scripts/setup-project.sh

# Ejecutar configuración
./scripts/setup-project.sh
```

### 3. **Configurar Variables de Entorno**

#### macOS/Linux:
```bash
# Agregar a ~/.bashrc o ~/.zshrc
export JAVA_HOME=/path/to/java
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

#### Windows:
```cmd
# Variables de sistema
JAVA_HOME=C:\Program Files\Java\jdk-17
ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
```

### 4. **Configurar Keystore**
```bash
# Copiar archivo de ejemplo
cp keystore.properties.example keystore.properties

# Editar con tus datos
nano keystore.properties
```

### 5. **Generar Keystore**
```bash
./scripts/generate-keystore.sh
```

### 6. **Verificar Instalación**
```bash
./scripts/troubleshoot.sh
```

## 🎯 Configuración de Android Studio

### 1. **Importar Proyecto**
- Abrir Android Studio
- File → Open → Seleccionar carpeta del proyecto
- Esperar sincronización de Gradle

### 2. **Configurar SDK**
- Tools → SDK Manager
- Instalar Android API 24+ (mínimo)
- Instalar Android API 36 (target)

### 3. **Configurar Emulador**
- Tools → AVD Manager
- Create Virtual Device
- Seleccionar dispositivo moderno
- Descargar imagen del sistema

## 📱 Configuración de Dispositivo Físico

### Android:
1. Habilitar "Opciones de desarrollador"
2. Activar "Depuración USB"
3. Conectar dispositivo
4. Verificar: `adb devices`

## 🔍 Verificación de la Configuración

### Compilación Debug:
```bash
./gradlew :composeApp:assembleDebug
```

### Compilación Release:
```bash
./scripts/build-release.sh
```

### Instalación en Dispositivo:
```bash
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## 🛠️ Configuración del IDE

### VS Code (Opcional):
```bash
# Instalar extensiones
code --install-extension ms-vscode.vscode-kotlin
code --install-extension vscjava.vscode-gradle
```

### IntelliJ IDEA:
- Instalar plugin de Kotlin Multiplatform
- Configurar SDK de Android

## 🔧 Configuración Avanzada

### Gradle Properties:
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
kotlin.code.style=official
```

### Configuración de Proxy (si es necesario):
```properties
# gradle.properties
systemProp.http.proxyHost=proxy.company.com
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=proxy.company.com
systemProp.https.proxyPort=8080
```

## 📊 Estructura del Proyecto

```
yapehub/
├── composeApp/              # Aplicación principal
│   ├── src/
│   │   ├── commonMain/      # Código compartido
│   │   ├── androidMain/     # Código Android
│   │   └── iosMain/         # Código iOS
│   └── build.gradle.kts
├── docs/                    # Documentación
├── scripts/                 # Scripts de automatización
├── keystores/              # Keystores (no en Git)
├── build.gradle.kts        # Configuración raíz
└── settings.gradle.kts     # Configuración de módulos
```

## 🚨 Solución de Problemas Comunes

### Error: "Java not found"
```bash
# Verificar instalación
java -version
javac -version

# Configurar JAVA_HOME
export JAVA_HOME=/path/to/java
```

### Error: "Android SDK not found"
```bash
# Configurar ANDROID_HOME
export ANDROID_HOME=/path/to/android/sdk

# Verificar
$ANDROID_HOME/tools/bin/sdkmanager --list
```

### Error: "Gradle sync failed"
```bash
# Limpiar y reintentar
./gradlew clean
./scripts/clean-project.sh
```

### Error: "Permission denied"
```bash
# Dar permisos a scripts
chmod +x scripts/*.sh
```

## 🎯 Próximos Pasos

Una vez configurado el proyecto:

1. **Desarrollo**: Lee la [Guía de Arquitectura](./ARCHITECTURE.md)
2. **Compilación**: Usa la [Guía de Scripts](./SCRIPT_EXECUTION_GUIDE.md)
3. **Distribución**: Consulta la [Guía de Play Store](./PLAY_STORE_GUIDE.md)

## 📞 Soporte

Si tienes problemas:
1. Ejecuta `./scripts/troubleshoot.sh`
2. Revisa los logs en `.kotlin/errors/`
3. Consulta la documentación específica
4. Crea un issue en el repositorio

---

**¡Configuración completada! Ya puedes empezar a desarrollar con YapeHub** 🚀