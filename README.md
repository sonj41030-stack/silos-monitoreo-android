# 🌾 Silos Monitoreo

Aplicación Android para **monitorear el alimento almacenado en silos**: consulta de niveles, alertas por estado, registro de movimientos (entradas y salidas) e historial. Los datos se conservan al cerrar la app. Desarrollada como proyecto de la asignatura **DSY1105**.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-local-003B57?logo=sqlite&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-build-2496ED?logo=docker&logoColor=white)

---

## 📋 Contenido

- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Usuarios de prueba](#-usuarios-de-prueba)
- [Datos de ejemplo](#-datos-de-ejemplo)
- [Cómo compilar la app](#-cómo-compilar-la-app)
  - [Opción A: con Android Studio](#opción-a-con-android-studio)
  - [Opción B: con Docker](#opción-b-con-docker)
- [Pruebas unitarias](#-pruebas-unitarias)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Autor](#-autor)

---

## ✨ Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Inicio de sesión** | Acceso por correo y contraseña, con validación de formato de correo. |
| **Roles** | Perfiles *Operador*, *Administrador* y *Jefatura*, cada uno con sus permisos. |
| **Niveles de silos** | Muestra el nivel actual de cada silo, agrupado por granja o planta. |
| **Alertas** | Calcula el estado de cada silo (normal, advertencia, crítico) según su nivel. |
| **Movimientos** | Registro de entradas y salidas de alimento. |
| **Validaciones** | Una salida no puede superar el stock disponible y una entrada no puede superar la capacidad del silo. |
| **Historial** | Lista de todos los movimientos registrados. |
| **Persistencia** | Los movimientos y el nivel de cada silo se guardan en SQLite y se conservan al cerrar la app. |

---

## 🛠 Tecnologías

- **Lenguaje:** Kotlin
- **Interfaz:** Jetpack Compose
- **Arquitectura:** MVVM (Model – View – ViewModel)
- **Base de datos local:** SQLite (tablas `usuarios`, `movimientos` y `niveles_silos`)
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
| Jefatura | `jefe@silos.com` | `jefe123` |

> ⚠️ Son credenciales de **prueba**, solo para desarrollo y demostración.

---

## 🗂 Datos de ejemplo

La app incluye datos iniciales de ejemplo: 3 granjas o plantas con 7 silos (maíz, soya, trigo, girasol, cebada, sorgo y maíz blanco) y algunos movimientos de muestra. Al registrar movimientos, el nivel del silo cambia y queda guardado.

---

## 🚀 Cómo compilar la app

### Opción A: con Android Studio

No necesitas Docker. Solo Android Studio y conexión a internet la primera vez.

1. En la pantalla de inicio, presiona **Clone Repository** (o **File → New → Project from Version Control**).
2. Pega la URL:
   ```
   https://github.com/sonj41030-stack/silos-monitoreo-android.git
   ```
3. Espera a que termine la sincronización de Gradle (acepta instalar el SDK de Android si lo pide).
4. Conecta un celular o inicia un emulador y presiona **Run ▶**.
5. Para generar el APK: **Build → Build APK(s)**.

Si ya tienes la carpeta en tu PC, usa **File → Open** y selecciona la carpeta raíz del proyecto (la que contiene `gradlew`).

### Opción B: con Docker

Sirve para generar el APK sin instalar Android Studio, el SDK ni Java: Docker trae todo.

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

> ⏱ La primera construcción tarda varios minutos porque descarga el SDK de Android. Si un test falla, el build se detiene y no entrega el APK.

> 💡 La app Android no corre dentro de Docker: Docker solo **compila** el APK, que luego se instala en el celular.

---

## 🧪 Pruebas unitarias

Las pruebas verifican la lógica de la app: validaciones de stock y capacidad, cálculo de estados y alertas, registro de movimientos, roles, y validación de correo y edad.

**Con Docker:** se ejecutan automáticamente durante `docker build`.

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
│       │   ├── data/
│       │   │   ├── api/       # Cliente y servicio de API
│       │   │   ├── auth/      # Autenticación y validación de cuentas
│       │   │   ├── db/        # SQLite (LocalDatabaseManager)
│       │   │   ├── DatosPrueba.kt
│       │   │   └── SilosRepository.kt
│       │   ├── model/         # Modelos (Silo, Granja, Movimiento, ...)
│       │   ├── navigation/    # Rutas de la app
│       │   ├── ui/            # Pantallas, componentes, tema y ViewModels
│       │   └── MainActivity.kt
│       └── test/              # Pruebas unitarias
├── gradle/                    # Configuración del wrapper de Gradle
├── Dockerfile                 # Receta para compilar el APK en Docker
├── .dockerignore              # Archivos que Docker no copia al contenedor
├── .gitattributes             # Mantiene gradlew con saltos de línea de Linux
├── build.gradle.kts           # Configuración de Gradle
├── settings.gradle.kts
└── README.md
```

---

## 👨‍💻 Autor

**Edixon Cabriles** · [@sonj41030-stack](https://github.com/sonj41030-stack)

Proyecto 
