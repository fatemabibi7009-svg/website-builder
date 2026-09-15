package com.example.server

import com.example.data.model.CompiledSite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ServerLogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val method: String,
    val path: String,
    val statusCode: Int,
    val clientIp: String,
    val responseBytes: Int
)

class WebsiteLocalServer {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _port = MutableStateFlow(8080)
    val port: StateFlow<Int> = _port.asStateFlow()

    private val _localIp = MutableStateFlow("127.0.0.1")
    val localIp: StateFlow<String> = _localIp.asStateFlow()

    private val _logs = MutableStateFlow<List<ServerLogEntry>>(emptyList())
    val logs: StateFlow<List<ServerLogEntry>> = _logs.asStateFlow()

    @Volatile
    private var currentSite: CompiledSite? = null

    fun updateSite(site: CompiledSite) {
        currentSite = site
    }

    suspend fun start(port: Int = 8080): Boolean = withContext(Dispatchers.IO) {
        if (_isRunning.value) return@withContext true
        try {
            _port.value = port
            _localIp.value = detectLocalIpAddress()
            val socket = ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))
            serverSocket = socket
            _isRunning.value = true

            addLog("SYSTEM", "/server-start", 200, "0.0.0.0:$port", 0)

            serverJob = scope.launch {
                while (isActive && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        launch { handleClient(client) }
                    } catch (e: Exception) {
                        if (socket.isClosed) break
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _isRunning.value = false
            false
        }
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        try {
            _isRunning.value = false
            serverJob?.cancel()
            serverSocket?.close()
            serverSocket = null
            addLog("SYSTEM", "/server-stop", 200, "local", 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress?.hostAddress ?: "unknown"
        try {
            socket.soTimeout = 5000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val out = socket.getOutputStream()

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return

            val method = parts[0]
            val fullPath = parts[1]
            val path = fullPath.substringBefore("?")

            val site = currentSite
            if (site == null) {
                sendResponse(out, 503, "text/plain", "Server is warming up...".toByteArray())
                addLog(method, path, 503, clientIp, 24)
                return
            }

            when (path) {
                "/", "/index.html" -> {
                    val bytes = site.html.toByteArray(Charsets.UTF_8)
                    sendResponse(out, 200, "text/html; charset=utf-8", bytes)
                    addLog(method, path, 200, clientIp, bytes.size)
                }
                "/styles.css", "/style.css" -> {
                    val bytes = site.css.toByteArray(Charsets.UTF_8)
                    sendResponse(out, 200, "text/css; charset=utf-8", bytes)
                    addLog(method, path, 200, clientIp, bytes.size)
                }
                "/main.js", "/app.js" -> {
                    val bytes = site.js.toByteArray(Charsets.UTF_8)
                    sendResponse(out, 200, "application/javascript; charset=utf-8", bytes)
                    addLog(method, path, 200, clientIp, bytes.size)
                }
                "/manifest.json" -> {
                    val bytes = site.manifestJson.toByteArray(Charsets.UTF_8)
                    sendResponse(out, 200, "application/json; charset=utf-8", bytes)
                    addLog(method, path, 200, clientIp, bytes.size)
                }
                "/health" -> {
                    val bytes = """{"status":"healthy","uptime":${System.currentTimeMillis()}}""".toByteArray()
                    sendResponse(out, 200, "application/json", bytes)
                    addLog(method, path, 200, clientIp, bytes.size)
                }
                else -> {
                    val notFound = "<h1>404 Not Found</h1><p>Resource $path was not found on this mobile local server.</p>".toByteArray()
                    sendResponse(out, 404, "text/html", notFound)
                    addLog(method, path, 404, clientIp, notFound.size)
                }
            }
        } catch (e: Exception) {
            // connection reset or timeout
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun sendResponse(out: OutputStream, code: Int, contentType: String, body: ByteArray) {
        val statusText = when (code) {
            200 -> "OK"
            404 -> "Not Found"
            503 -> "Service Unavailable"
            else -> "OK"
        }
        val header = "HTTP/1.1 $code $statusText\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${body.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.US_ASCII))
        out.write(body)
        out.flush()
    }

    private fun addLog(method: String, path: String, status: Int, clientIp: String, bytes: Int) {
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = ServerLogEntry(
            timestamp = timeFmt,
            method = method,
            path = path,
            statusCode = status,
            clientIp = clientIp,
            responseBytes = bytes
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _logs.value = current
    }

    private fun detectLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }
}
