# 📚 YapeChamoApp - Documentación

## 🚀 Acceso Rápido

Toda la documentación del proyecto se encuentra organizada en la carpeta [`docs/`](./docs/):

### 📖 **[Ver Documentación Completa →](./docs/README.md)**

## 📋 Documentos Principales

| Documento | Descripción |
|-----------|-------------|
| **[📚 Índice General](./docs/README.md)** | Punto de entrada a toda la documentación |
| **[🚀 CI/CD Setup](./docs/CI_CD_SETUP.md)** | Configuración de GitHub Actions |
| **[🔐 GitHub Secrets](./docs/GITHUB_SECRETS.md)** | Configuración de secrets para CI/CD |
| **[✅ Google Play Checklist](./docs/GOOGLE_PLAY_CHECKLIST.md)** | Guía para publicar en Play Store |
| **[🌍 Environment Config](./docs/ENVIRONMENT_CONFIG.md)** | Configuración de entornos |

## 🎯 Quick Start

### Para desarrolladores nuevos:
```bash
# 1. Leer documentación de build
open docs/BUILD_GUIDE.md

# 2. Configurar entorno
bash scripts/setup-project.sh

# 3. Build de desarrollo
bash scripts/build-all.sh -t apk
```

### Para deployment:
```bash
# 1. Configurar CI/CD
open docs/CI_CD_SETUP.md

# 2. Configurar secrets
bash scripts/setup-github-secrets.sh

# 3. Deploy automático
git commit -m "Release v1.1.0 [deploy]"
```

---

**📁 Ubicación**: Toda la documentación está en [`docs/`](./docs/)  
**🔄 Actualización**: La documentación se mantiene actualizada con cada release