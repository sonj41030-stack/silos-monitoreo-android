# 🌾 Silos Monitoreo

Aplicación Android para **monitorear el alimento almacenado en silos**: consulta de niveles, alertas por estado, registro de movimientos (entradas y salidas) e historial. Desarrollada como proyecto de la asignatura **DSY1105**.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-build-2496ED?logo=docker&logoColor=white)

---

## 📋 Contenido

- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Usuarios de prueba](#-usuarios-de-prueba)
- [Cómo compilar la app](#-cómo-compilar-la-app)
  - [Opción A: con Docker](#opción-a-con-docker-recomendada)
  - [Opción B: con Android Studio](#opción-b-con-android-studio)
- [Pruebas unitarias](#-pruebas-unitarias)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Problemas conocidos](#-problemas-conocidos)
- [Autor](#-autor)

---

## ✨ Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Inicio de sesión** | Acceso por correo y contraseña, con validación de formato de correo. |
| **Roles** | Perfil *Operador* y perfil *Administrador*, cada uno con sus permisos. |
| **Niveles de silos** | Muestra el nivel actual de cada silo con su capacidad máxima. |
| **Alertas** | Calcula el estado de cada silo (normal, bajo, crítico, etc.) según su nivel. |
| **Movimientos** | Registro de entradas y salidas de alimento. |
| **Validaciones** | Una salida no puede superar el stock disponible y una entrada no puede superar la capacidad del silo. |
| **Historial** | Lista de todos los movimientos registrados. |

---

## 🛠 Tecnologías

- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose
- **Arquitectura:** MVVM (Model – View – ViewModel)
- **Base de datos local:** SQLite
- **Compilación:** Gradle (Kotlin DSL)
- **Entorno reproducible:** Docker
- **Pruebas:** JUnit (pruebas unitarias)

---

## 👤 Usuarios de prueba

Cuentas de demostración para probar la app:

| Rol | Correo | Contraseña |
|---|---|---|
| Operador | `operador@silos.com` | `operador123` |
| Administrador | `admin@silos.com` | `admin123` |

> ⚠️ Son credenciales de **prueba**, solo para desarrollo y demostración.

---

## 🚀 Cómo compilar la app

### Opción A: con Docker (recomendada)

No necesitas instalar Android Studio, el SDK ni Java: Docker trae todo.

**Requisitos:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y **abierto**.

```bash
# 1. Clonar el repositorio
git clone https://github.com/sonj41030-stack/silos-monitoreo-android.git

# 2. Entrar a la carpeta del proyecto
cd silos-monitoreo-android

# 3. Construir la imagen (ejecuta los tests y compila el APK)
docker build -t silos-apk .

# 4. Sacar el APK del contenedor
docker create --name silos-temp silos-apk
docker cp silos-temp:/app/app/build/outputs/apk/debug/app-debug.apk .
docker rm silos-temp
```

Al terminar tendrás `app-debug.apk` en tu carpeta, listo para instalar en un celular Android.

> ⏱ La primera construcción tarda varios minutos porque descarga el SDK de Android. Las siguientes son mucho más rápidas.

**Instalar en el celular:** copia `app-debug.apk` al teléfono, ábrelo y permite la instalación de orígenes desconocidos si el sistema lo pide.

### Opción B: con Android Studio

1. Clona el repositorio:
   ```bash
   git clone https://github.com/sonj41030-stack/silos-monitoreo-android.git
   ```
2. Abre la carpeta en **Android Studio**.
3. Espera a que termine la sincronización de Gradle.
4. Conecta un dispositivo o inicia un emulador y presiona **Run ▶**.
5. Para generar el APK: **Build → Build APK(s)**.

---

## 🧪 Pruebas unitarias

Las pruebas verifican la lógica de la app: validaciones de stock y capacidad, cálculo de estados y alertas, registro de movimientos, roles, y validación de correo y edad.

**Con Docker:** se ejecutan automáticamente durante `docker build`. Si alguna falla, la construcción se detiene.

**Con Gradle:**

```bash
./gradlew testDebugUnitTest
```

En Windows (PowerShell): `.\gradlew testDebugUnitTest`

> Las pruebas cubren la lógica, no las pantallas, la navegación ni la base de datos. Para eso, prueba la app en un dispositivo.

---

## 📁 Estructura del proyecto

```
silos-monitoreo-android/
├── app/
│   └── src/
│       ├── main/java/com/sonj/silosmonitoreo/
│       │   ├── data/          # Repositorio y acceso a datos
│       │   ├── ui/
│       │   │   └── viewmodel/ # ViewModels (lógica de pantalla)
│       │   └── ...            # Pantallas y modelos
│       └── test/              # Pruebas unitarias
├── gradle/                    # Configuración del wrapper de Gradle
├── Dockerfile                 # Receta para compilar el APK en Docker
├── .dockerignore              # Archivos que Docker no copia al contenedor
├── build.gradle.kts           # Configuración de Gradle
├── settings.gradle.kts
└── README.md
```

---

## ⚠️ Problemas conocidos

- Los niveles de los silos **no se conservan al cerrar la app** (persistencia pendiente de corregir).

---

## 👨‍💻 Autor

**Edixon Cabriles** · [@sonj41030-stack](https://github.com/sonj41030-stack)

Proyecto académico · DSY1105
