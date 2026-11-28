# Silos Monitoreo (App Android)

App Android para monitorear el alimento almacenado en silos: niveles, alertas por estado, registro de movimientos (entradas y salidas), historial y CRUD de silos. Kotlin + Jetpack Compose + MVVM, conectada a microservicios Spring Boot propios y a una API externa de clima. Proyecto de la asignatura DSY1105.

Repositorio del backend (microservicios): https://github.com/sonj41030-stack/silos-monitoreo-backend

## Integrantes

- Edixon Cabriles (sonj41030-stack). Proyecto desarrollado de forma individual, con conocimiento del docente.

## Funcionalidades

| Modulo | Que hace |
|---|---|
| Inicio de sesion y registro | Login con JWT contra ms-auth. Registro con fecha de nacimiento (mayores de 18). |
| Roles | Operador, Administrador y Jefatura, cada uno con su vista y permisos. |
| Niveles y alertas | Lista de granjas y silos con nivel, estado (normal, advertencia, critico) y centro de alertas. |
| CRUD de silos | Solo Administrador: crear, editar y eliminar (borrado logico) con validaciones de formulario. |
| Movimientos | Entradas y salidas que ajustan el nivel del silo (ms-silos) y se guardan en el historial (ms-movimientos). |
| Historial | Lista y filtros por tipo de movimiento. |
| Clima ambiente | Tarjeta con temperatura y humedad desde una API externa (Open-Meteo). |
| Dashboard Jefatura | Resumen ejecutivo y ocupacion global. |

## Endpoints utilizados

### API externa (Retrofit, sin JWT, HTTPS)

| Metodo | URL | Uso |
|---|---|---|
| GET | https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,relative_humidity_2m | Clima mostrado en la lista de silos |

### Microservicios propios (JWT en `Authorization: Bearer <token>`)

| Servicio | Puerto | Metodo y ruta |
|---|---|---|
| ms-auth | 8081 | POST /api/auth/login, POST /api/auth/register, GET /api/auth/me |
| ms-silos | 8082 | GET /api/granjas, POST /api/silos, PUT /api/silos/{id}, DELETE /api/silos/{id}, POST /api/silos/{id}/movimientos |
| ms-movimientos | 8083 | POST /api/movimientos, GET /api/movimientos?siloId= |

El detalle completo de cada endpoint esta en el README del backend.

## Usuarios de prueba

| Rol | Correo | Contrasena |
|---|---|---|
| Operador | operador@silos.com | operador123 |
| Administrador | admin@silos.com | admin123 |
| Jefatura | jefe@silos.com | jefe123 |

Credenciales de prueba, solo para desarrollo y evaluacion.

## Pasos para ejecutar

Requisitos: Android Studio, JDK 17 o superior, Docker Desktop.

1. Clonar el backend y levantar la base de datos y los servicios (ver README del backend):
   - `docker compose up -d`
   - ms-auth: `cd ms-auth` y `.\mvnw spring-boot:run`
   - ms-silos: `cd ms-silos` y `.\gradlew.bat bootRun`
   - ms-movimientos: `cd ms-movimientos` y `.\gradlew.bat bootRun`
2. Clonar esta app: `git clone https://github.com/sonj41030-stack/silos-monitoreo-android.git`
3. Abrir la carpeta en Android Studio (File > Open), esperar el Gradle Sync y ejecutar en un emulador con Run. El emulador accede al PC por `10.0.2.2`.
4. O instalar el APK firmado directamente: `adb install -r release\silos-monitoreo.apk`

## APK firmado y llave (.jks)

- APK firmado: `release/silos-monitoreo.apk`
- Llave: `keystore/silos-release.jks` (alias `silos`, contrasena `silos2026`, solo para esta evaluacion)
- Configuracion: `keystore.properties` y bloque `signingConfigs` en `app/build.gradle.kts`
- Generar: `.\gradlew.bat assembleRelease`

Captura de la llave (keytool):

![keytool](docs/capturas/keytool.png)

Verificacion de la firma del APK (apksigner):

![apksigner](docs/capturas/apksigner.png)

Llave dentro del repositorio:

![keystore](docs/capturas/keystore.png)

## Pruebas unitarias

JUnit + MockK + coroutines-test sobre Repository, ViewModels, validadores y mappers. Cobertura con JaCoCo: 95 % de instrucciones y 81 % de ramas.




El reporte queda en `app/build/reports/jacoco/jacocoTestReport/html/index.html`.

## Estructura