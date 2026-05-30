package com.example.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class UpdateService(private val context: Context) {

    private val client = OkHttpClient()

    data class ReleaseInfo(
        val isNewer: Boolean,
        val versionName: String,
        val description: String,
        val downloadUrl: String
    )

    // Sincroniza y chequea la última versión en GitHub
    suspend fun checkLatestRelease(currentVersionName: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/williamsgp71/controlDeGastosAPP/releases/latest")
                .header("User-Agent", "control-de-gastos-updater")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)

                val latestTag = json.optString("tag_name", "v1.0.0").replace("v", "") // ej: "1.1.0"
                val body = json.optString("body", "Nuevas mejoras de rendimiento.")
                val assets = json.optJSONArray("assets") ?: return@withContext null
                
                var apkUrl = ""
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }

                if (apkUrl.isBlank()) return@withContext null

                // Compare version names
                val isNewer = isVersionNewer(latestTag, currentVersionName)

                ReleaseInfo(
                    isNewer = isNewer,
                    versionName = latestTag,
                    description = body,
                    downloadUrl = apkUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.replace("v", "").split(".").mapNotNull { it.toIntOrNull() }

            val minSize = minOf(latestParts.size, currentParts.size)
            for (i in 0 until minSize) {
                if (latestParts[i] > currentParts[i]) return true
                if (latestParts[i] < currentParts[i]) return false
            }
            return latestParts.size > currentParts.size
        } catch (e: Exception) {
            return latest != current
        }
    }

    fun downloadAndInstallApk(url: String, fileName: String = "control_gastos_update.apk") {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            
            // Delete old download if exists
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("Actualizando Control de Gastos")
                setDescription("Descargando nueva versión...")
                setDestinationUri(Uri.fromFile(destinationFile))
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setMimeType("application/vnd.android.package-archive")
            }

            val downloadId = downloadManager.enqueue(request)

            // Register broadcast receiver to automatically open the installer upon complete
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                    if (id == downloadId) {
                        context.unregisterReceiver(this)
                        installApk(destinationFile)
                    }
                }
            }

            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installApk(apkFile: File) {
        try {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
