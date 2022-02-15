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
        try {
            //executing the network request
            val call = client.newCall(Request.Builder().url(url).get().build())
            val response: Response = call.execute()
            if (response.code != 200 || response.body == null) return false

            //preparing the input and output stream
            val inputStream = response.body!!.byteStream()
            val outputStream = FileOutputStream(File(downloadPath))

            //preparing the buffer and the progress status
            val buffer = ByteArray(1024 * 4)
            var downloadedBytes: Long = 0
            val totalBytes: Long = response.body!!.contentLength()

            //calling progress if necessary
            withContext(Dispatchers.Main){ progress?.invoke(downloadedBytes, totalBytes) }

            var readBytes: Int = 0
            while (readBytes != -1) {
                //read from input
                readBytes = inputStream.read(buffer)
                if (readBytes == -1) break

                //write to output
                outputStream.write(buffer)

                //update progress
                downloadedBytes += readBytes.toLong()
                withContext(Dispatchers.Main){
                    progress?.invoke(downloadedBytes, totalBytes)
                }
            }

            //closing the streams
            inputStream.close()
            outputStream.flush()
            outputStream.close()

            return downloadedBytes == totalBytes



        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    val client = OkHttpClient()
}