package com.example.deploy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class NetlifyUser(
    val id: String,
    val email: String,
    val fullName: String,
    val slug: String
)

data class NetlifyDeployResult(
    val siteId: String,
    val siteName: String,
    val liveUrl: String,
    val adminUrl: String,
    val isSimulation: Boolean = false
)

sealed class DeployProgressState {
    object Idle : DeployProgressState()
    data class Progress(val step: String, val percentage: Float) : DeployProgressState()
    data class Success(val result: NetlifyDeployResult) : DeployProgressState()
    data class Error(val message: String) : DeployProgressState()
}

class NetlifyDeployer {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun verifyToken(token: String): Result<NetlifyUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.netlify.com/api/v1/user")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Netlify Auth Error: HTTP ${response.code} ${response.message}"))
                }
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val user = NetlifyUser(
                    id = json.optString("id", ""),
                    email = json.optString("email", ""),
                    fullName = json.optString("full_name", json.optString("slug", "Netlify Developer")),
                    slug = json.optString("slug", "")
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deployReal(
        token: String,
        siteName: String?,
        zipFile: File,
        onProgress: (DeployProgressState) -> Unit
    ): Result<NetlifyDeployResult> = withContext(Dispatchers.IO) {
        try {
            onProgress(DeployProgressState.Progress("Preparing distribution bundle...", 0.10f))
            if (!zipFile.exists() || zipFile.length() == 0L) {
                val err = "Zip distribution file not found or empty."
                onProgress(DeployProgressState.Error(err))
                return@withContext Result.failure(Exception(err))
            }

            val safeName = siteName?.trim()?.lowercase()?.replace(Regex("[^a-z0-9-]"), "-")?.trim('-')
            var targetSiteId: String? = null
            var targetSiteName: String? = null

            // If user specified a custom site name, create or locate the named site first
            if (!safeName.isNullOrBlank()) {
                onProgress(DeployProgressState.Progress("Configuring Netlify site '$safeName'...", 0.25f))

                // Check existing sites in user's Netlify account
                try {
                    val listReq = Request.Builder()
                        .url("https://api.netlify.com/api/v1/sites?filter=all")
                        .header("Authorization", "Bearer $token")
                        .get()
                        .build()

                    client.newCall(listReq).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val listBody = resp.body?.string() ?: ""
                            val sitesArr = org.json.JSONArray(listBody)
                            for (i in 0 until sitesArr.length()) {
                                val s = sitesArr.getJSONObject(i)
                                if (s.optString("name").equals(safeName, ignoreCase = true)) {
                                    targetSiteId = s.optString("id")
                                    targetSiteName = s.optString("name")
                                    break
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore list errors and continue with creation attempt
                }

                // If site doesn't exist under user's account, create it with the custom name
                if (targetSiteId == null) {
                    onProgress(DeployProgressState.Progress("Reserving subdomain '$safeName.netlify.app'...", 0.40f))
                    val jsonBody = JSONObject().put("name", safeName).toString()
                    val createReq = Request.Builder()
                        .url("https://api.netlify.com/api/v1/sites")
                        .header("Authorization", "Bearer $token")
                        .header("Content-Type", "application/json")
                        .post(okhttp3.RequestBody.create("application/json".toMediaType(), jsonBody))
                        .build()

                    client.newCall(createReq).execute().use { resp ->
                        val respStr = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            val createdJson = JSONObject(respStr)
                            targetSiteId = createdJson.optString("id")
                            targetSiteName = createdJson.optString("name", safeName)
                        } else if (resp.code == 422) {
                            val err = "The site name '$safeName' is already taken on Netlify. Please choose another unique name."
                            onProgress(DeployProgressState.Error(err))
                            return@withContext Result.failure(Exception(err))
                        }
                    }
                }
            }

            // Determine upload endpoint: deploy to specific site if we have siteId, or create with zip
            val deployUrl = if (!targetSiteId.isNullOrBlank()) {
                "https://api.netlify.com/api/v1/sites/$targetSiteId/deploys"
            } else if (!safeName.isNullOrBlank()) {
                "https://api.netlify.com/api/v1/sites?name=$safeName"
            } else {
                "https://api.netlify.com/api/v1/sites"
            }

            onProgress(DeployProgressState.Progress("Uploading bundle to Netlify Edge CDN...", 0.65f))
            val mediaType = "application/zip".toMediaType()
            val requestBody = zipFile.asRequestBody(mediaType)

            val request = Request.Builder()
                .url(deployUrl)
                .header("Authorization", "Bearer $token")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                onProgress(DeployProgressState.Progress("Configuring DNS and generating SSL certificate...", 0.90f))
                val respBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val err = "Deploy Failed (HTTP ${response.code}): $respBody"
                    onProgress(DeployProgressState.Error(err))
                    return@withContext Result.failure(Exception(err))
                }

                val json = JSONObject(respBody)
                val siteId = json.optString("site_id", json.optString("id", targetSiteId ?: ""))
                val name = json.optString("name", targetSiteName ?: safeName ?: "site-$siteId")
                val liveUrl = json.optString("ssl_url", json.optString("url", "https://$name.netlify.app"))
                val adminUrl = json.optString("admin_url", "https://app.netlify.com/sites/$name")

                val result = NetlifyDeployResult(
                    siteId = siteId,
                    siteName = name,
                    liveUrl = liveUrl,
                    adminUrl = adminUrl,
                    isSimulation = false
                )
                onProgress(DeployProgressState.Success(result))
                Result.success(result)
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown deployment network error"
            onProgress(DeployProgressState.Error(msg))
            Result.failure(e)
        }
    }

    suspend fun deploySimulation(
        siteSlug: String,
        zipFile: File,
        onProgress: (DeployProgressState) -> Unit
    ): Result<NetlifyDeployResult> = withContext(Dispatchers.IO) {
        try {
            onProgress(DeployProgressState.Progress("Compiling dist files & verifying index.html...", 0.20f))
            delay(500)
            onProgress(DeployProgressState.Progress("Connecting to Netlify Global Edge Network...", 0.45f))
            delay(600)
            onProgress(DeployProgressState.Progress("Uploading ${zipFile.length() / 1024} KB distribution bundle...", 0.70f))
            delay(600)
            onProgress(DeployProgressState.Progress("Reserving subdomain & generating SSL certificate...", 0.90f))
            delay(500)

            val safeSlug = siteSlug.trim().lowercase().replace(Regex("[^a-z0-9-]"), "-").trim('-')
            val finalName = if (safeSlug.isNotBlank()) {
                safeSlug
            } else {
                val randSuffix = Random.nextInt(1000, 9999)
                "quick-site-$randSuffix"
            }
            val liveUrl = "https://$finalName.netlify.app"
            val adminUrl = "https://app.netlify.com/sites/$finalName"

            val result = NetlifyDeployResult(
                siteId = "sim_${finalName.hashCode().let { kotlin.math.abs(it) }}",
                siteName = finalName,
                liveUrl = liveUrl,
                adminUrl = adminUrl,
                isSimulation = true
            )
            onProgress(DeployProgressState.Success(result))
            Result.success(result)
        } catch (e: Exception) {
            val msg = e.message ?: "Simulation error"
            onProgress(DeployProgressState.Error(msg))
            Result.failure(e)
        }
    }
}
