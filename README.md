# 🚀 Guía de Configuración y Ejecución: Control de Gastos (Android)

Esta guía describe el paso a paso detallado para configurar el entorno de desarrollo y levantar el proyecto de **Control de Gastos** en un sistema **Windows** utilizando la consola del IDE.

---

## 📋 Requisitos Previos

Asegúrate de contar con los siguientes elementos instalados y configurados. Todos pueden ser instalados directamente desde la consola usando comandos automáticos.

### 1. Instalar Java JDK 17
La compilación del proyecto Android mediante Gradle requiere Java 17. Puedes instalar la versión oficial de **Eclipse Temurin** de manera silenciosa:

```powershell
winget install --id EclipseAdoptium.Temurin.17.JDK --silent --accept-package-agreements --accept-source-agreements
```

* **Ruta de instalación por defecto**: `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot`

### 2. Instalar Android Studio y el SDK
Para obtener las herramientas de compilación, emuladores y plataformas de desarrollo oficiales, instala **Android Studio**:

```powershell
winget install --id Google.AndroidStudio --silent --accept-package-agreements --accept-source-agreements
```

> [!IMPORTANT]  
> **Primer inicio**: Después de la instalación, abre Android Studio una vez por el menú de inicio de Windows. Completa el Asistente de Configuración (Setup Wizard) inicial seleccionando la opción **Standard**. Esto descargará automáticamente el SDK y las herramientas en la ruta:  
> `C:\Users\Desktop\AppData\Local\Android\Sdk`

---

## 🛠️ Configuración del Proyecto

### 1. Configurar el Archivo `local.properties`
Indica al compilador de Gradle dónde se encuentra el SDK en tu máquina Windows. Abre o crea el archivo `local.properties` en la raíz del proyecto y agrega la siguiente línea:

```properties
sdk.dir=C\:\\Users\\Desktop\\AppData\\Local\\Android\\Sdk
```

### 2. Decodificar el Keystore de Depuración (debug.keystore)
El proyecto utiliza una firma de desarrollo que viene cifrada en base64. Decodifícala ejecutando el siguiente comando en la raíz del proyecto:

```powershell
certutil -decode debug.keystore.base64 debug.keystore
```

---

## ⚙️ Compilar el Proyecto

Genera el instalador oficial de depuración (APK) utilizando el Gradle Wrapper configurado con tu versión de Java 17:

```powershell
# 1. Configura la variable temporal de Java en tu sesión de terminal
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"

# 2. Compila el APK de depuración
.\gradlew.bat assembleDebug
```

El APK resultante se generará en la siguiente ruta:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Levantar el Emulador y Desplegar la Aplicación

Para emular y ejecutar el proyecto desde la terminal del IDE, utilizaremos la herramienta oficial de comandos **Android CLI**.

### 1. Instalar la herramienta Android CLI
Instala la extensión de comandos del IDE:

```powershell
cmd.exe /c "curl.exe -fsSL https://dl.google.com/android/cli/latest/windows_x86_64/install.cmd -o %TEMP%\i.cmd && %TEMP%\i.cmd"
```

### 2. Crear un Dispositivo Virtual (Emulador)
Crea una máquina virtual Android utilizando el perfil de teléfono mediano:

```powershell
& "C:\Users\Desktop\AppData\AndroidCLI\android.exe" emulator create medium_phone
```

### 3. Iniciar el Emulador
Arranca el emulador de forma independiente en segundo plano en Windows:

```powershell
Start-Process -FilePath "C:\Users\Desktop\AppData\Local\Android\Sdk\emulator\emulator.exe" -ArgumentList "-avd", "medium_phone"
```

*Espera unos segundos a que la pantalla del emulador cargue el sistema operativo completamente.*

### 4. Desplegar e Instalar el APK en el Emulador
Una vez que el emulador esté encendido (puedes verificar si está listo con `& "C:\Users\Desktop\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices`), instala y arranca la aplicación:

```powershell
# Despliega el APK recién compilado
& "C:\Users\Desktop\AppData\AndroidCLI\android.exe" run --apks=app/build/outputs/apk/debug/app-debug.apk
```

¡Listo! La aplicación **Control de Gastos** se abrirá automáticamente en la pantalla de tu emulador mostrando la pantalla de inicio de sesión.

---

## 📲 Instalación en Dispositivo Android Físico

Si prefieres probar la aplicación directamente en tu celular o tablet física, sigue estos pasos:

### 1. Preparar tu Teléfono Android
1. Abre **Ajustes** en tu teléfono ➔ **Información del teléfono** ➔ Pulsa **7 veces** sobre **Número de compilación** para activar el *"Modo Desarrollador"*.
2. Vuelve a **Ajustes** ➔ **Sistema** ➔ **Opciones de desarrollador** y activa la opción **Depuración por USB**.
3. Conecta tu teléfono a la computadora mediante cable USB.
4. Si aparece una ventana emergente en la pantalla de tu teléfono solicitando permisos de depuración, selecciona **"Permitir siempre desde esta computadora"** y pulsa **Aceptar**.

### 2. Instalar el APK desde la Consola
Verifica que la computadora reconoce tu dispositivo ejecutando:

```powershell
& "C:\Users\Desktop\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
```

Instala la aplicación en tu dispositivo:

* **Si tu celular es el único dispositivo activo:**
  ```powershell
  & "C:\Users\Desktop\AppData\Local\Android\Sdk\platform-tools\adb.exe" install app/build/outputs/apk/debug/app-debug.apk
  ```

* **Si tienes el emulador y tu celular conectados al mismo tiempo (Conflicto de múltiples dispositivos):**
  ADB dará el error `more than one device/emulator`. Para resolverlo, obtén el identificador de tu teléfono (por ejemplo `a94227f`) con el comando anterior y ejecútalo especificándolo con `-s`:
  ```powershell
  & "C:\Users\Desktop\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s a94227f install app/build/outputs/apk/debug/app-debug.apk
  ```

---

## 🧹 Apagar y Liberar Recursos de la Computadora

Cuando termines tu sesión de desarrollo y quieras liberar memoria RAM y recursos de tu computadora, ejecuta estos comandos rápidos en la consola del IDE:

### 1. Apagar los Demonios de Gradle (Libera 1 GB a 2 GB de memoria RAM)
Gradle mantiene procesos en segundo plano para agilizar futuras compilaciones, lo que puede consumir bastantes recursos. Puedes cerrarlos por completo ejecutando:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
.\gradlew.bat --stop
```

### 2. Detener el Servidor de ADB (Libera puertos USB y de red)
Cierra el servidor de depuración de Android que interactúa con tu teléfono o emulador:

```powershell
& "C:\Users\Desktop\AppData\Local\Android\Sdk\platform-tools\adb.exe" kill-server
```

### 3. Apagar el Emulador Android
Si tienes un emulador en ejecución y deseas cerrarlo forzosamente desde la terminal:

```powershell
Stop-Process -Name "emulator" -Force
```
