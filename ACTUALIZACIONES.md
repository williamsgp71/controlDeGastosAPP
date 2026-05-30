# 📱 Guía del Desarrollador: Cómo Enviar Actualizaciones Inalámbricas (OTA)

Esta guía explica el procedimiento paso a paso para publicar una nueva versión de la aplicación **Control de Gastos** de modo que tus familiares la reciban de forma automática dentro de la app y puedan instalarla con un solo toque.

---

## 🛠️ Paso 1: Incrementar la Versión en el Código

Antes de compilar la nueva actualización, debes indicarle a la app que es una versión más reciente.

1. Abre el archivo `app/build.gradle.kts` en tu IDE.
2. Localiza el bloque `defaultConfig` y aumenta los valores de:
   * **`versionCode`**: Increméntalo en `1` (ej. si estaba en `1`, cámbialo a `2`).
   * **`versionName`**: Cambia la cadena de texto a la nueva versión semántica (ej. de `"1.0"` a `"1.1"` o `"1.1.0"`).

**Ejemplo de cambio:**
```kotlin
  defaultConfig {
    applicationId = "com.aistudio.controldegastos.pvwyzk"
    minSdk = 24
    targetSdk = 36
    versionCode = 2        // ➔ Aumentado de 1 a 2
    versionName = "1.1.0"  // ➔ Siguiente versión
    
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
```

---

## ⚙️ Paso 2: Compilar el APK de Depuración o Producción

Compila el archivo de instalación final desde la consola del IDE utilizando Gradle:

```powershell
# 1. Configura la variable de Java JDK 17
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"

# 2. Compila el APK limpio
.\gradlew.bat assembleDebug
```

El archivo APK resultante para compartir se generará en la siguiente ruta:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 Paso 3: Publicar la Actualización en GitHub

Dado que la aplicación lee el API de GitHub en tiempo real, solo necesitas crear un "Release" público y adjuntar el APK compiled:

1. Entra a tu repositorio en GitHub desde el navegador:  
   👉 [https://github.com/williamsgp71/controlDeGastosAPP](https://github.com/williamsgp71/controlDeGastosAPP)
2. En la columna derecha, busca la sección **Releases** y haz clic en **"Create a new release"** (o *"Draft a new release"*).
3. **Choose a tag**: Escribe la versión con una `"v"` al inicio (ej. `v1.1.0`). *Asegúrate de que coincida con el versionName de tu Gradle*.
4. **Release Title**: Ponle un nombre descriptivo a la actualización (ej. `Actualización v1.1.0: Mejoras de UI`).
5. **Description**: Escribe una lista con las novedades. Tus familiares verán este texto en la pantalla de su celular al presionar actualizar. (ej. *"Añadida pestaña de Seguridad Biométrica, corregida conversión de monedas"*).
6. **Attach binaries**: Arrastra y suelta el archivo `app-debug.apk` compiled en la sección gris de archivos adjuntos.
7. Haz clic en el botón verde **"Publish release"**.

---

## 📲 Paso 4: Cómo lo Reciben Tus Familiares

Una vez que publicas el release en GitHub:
1. Tus familiares abren la aplicación en su teléfono móvil.
2. Van a la pestaña de **Seguridad Biométrica** y presionan el botón **"Buscar"** en la sección **Actualizaciones de la Aplicación**.
3. Inmediatamente aparecerá un diálogo modal *Cosmic Dark* en la pantalla con el mensaje:  
   `¡Nueva Actualización! Versión disponible: v1.1.0`
4. Podrán leer la lista de novedades que escribiste en el paso anterior.
5. Al presionar el botón morado **"Actualizar"**:
   * La app descargará en segundo plano el APK desde GitHub usando el administrador nativo de Android.
   * Al finalizar la descarga, se abrirá la pantalla del sistema preguntando: *"¿Deseas instalar una actualización de esta aplicación existente?"*.
   * Presionan **Instalar** y ¡listo! La aplicación se actualiza de forma transparente conservando todos sus datos históricos.
