# ============================================================
# Dockerfile para COMPILAR el APK de SilosMonitoreo
# Docker no ejecuta la app: solo la construye en un entorno limpio
# ============================================================

# 1) IMAGEN BASE
# Todo Dockerfile parte de una imagen existente. Esta trae Java 17 (JDK),
# que Gradle y el plugin de Android necesitan para compilar.
FROM eclipse-temurin:17-jdk

# 2) VARIABLES DE ENTORNO
# ANDROID_HOME: carpeta donde vamos a instalar el SDK de Android.
# Gradle la usa para encontrar las herramientas de compilación.
ENV ANDROID_HOME=/opt/android-sdk

# PATH: lista de carpetas donde el sistema busca comandos.
# Agregamos la de cmdline-tools para poder usar el comando "sdkmanager".
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# 3) HERRAMIENTAS DEL SISTEMA
# wget descarga archivos y unzip los descomprime.
# "rm -rf /var/lib/apt/lists/*" borra la caché de apt para que la imagen pese menos.
RUN apt-get update && apt-get install -y unzip wget && rm -rf /var/lib/apt/lists/*

# 4) INSTALAR LAS COMMAND LINE TOOLS DE ANDROID
# Son las herramientas de Google sin interfaz gráfica (Android Studio las trae
# incluidas, pero aquí no hay Android Studio).
# Pasos: crear la carpeta, descargar el zip, descomprimirlo, renombrar la carpeta
# a "latest" (la ruta que espera sdkmanager) y borrar el zip.
RUN mkdir -p $ANDROID_HOME/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/tools.zip && \
    unzip -q /tmp/tools.zip -d $ANDROID_HOME/cmdline-tools && \
    mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest && \
    rm /tmp/tools.zip

# 5) INSTALAR EL SDK
# "yes |" responde "sí" automáticamente a las licencias, porque en un build
# no hay nadie para aceptarlas a mano.
# platform-tools: herramientas básicas (adb, etc.).
# platforms;android-35: la plataforma contra la que compila tu app (compileSdk).
# build-tools: las herramientas que empaquetan el APK.
# IMPORTANTE: el 35 debe coincidir con el compileSdk de tu app/build.gradle.kts.
RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

# 6) CARPETA DE TRABAJO
# Desde aquí en adelante, todos los comandos se ejecutan dentro de /app.
WORKDIR /app

# 7) COPIAR EL PROYECTO
# Primer punto: todo lo de tu carpeta actual (la raíz del proyecto).
# Segundo punto: el destino, o sea /app por el WORKDIR.
COPY . .

# 8) COMPILAR
# chmod +x: da permiso de ejecución a gradlew (si no, falla con "Permission denied").
# assembleDebug: genera el APK de depuración.
# --no-daemon: evita dejar un proceso de Gradle en segundo plano dentro de la imagen.
RUN chmod +x gradlew && ./gradlew testDebugUnitTest assembleDebug --no-daemon