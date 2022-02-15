package dev.cele.igdownloader.activities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object HttpClient {
    suspend fun download(url: String, downloadPath: String, progress : ((Long, Long) -> Unit)? = null): Boolean {
        val call = client.newCall(Request.Builder().url(url).get().build())
        try {
            val response: Response = call.execute()
            if (response.code != 200 || response.body == null) return false

            response.body!!.byteStream().use{ inputStream ->
                FileOutputStream(File(downloadPath)).use { outputStream ->

                    val buff = ByteArray(1024 * 4)

                    var downloadedBytes: Long = 0
                    val totalBytes: Long = response.body!!.contentLength()
                    withContext(Dispatchers.Main){
                        progress?.invoke(downloadedBytes, totalBytes)
                    }
                    while (true) {
                        val readBytes: Int = inputStream.read(buff)
                        if (readBytes == -1) {
                            break
                        }
                        //write buff
                        outputStream.write(buff)
                        downloadedBytes += readBytes.toLong()
                        withContext(Dispatchers.Main){
                            progress?.invoke(downloadedBytes, totalBytes)
                        }
                    }
                    return downloadedBytes == totalBytes

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    val client = OkHttpClient()
}