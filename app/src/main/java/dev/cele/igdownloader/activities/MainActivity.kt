package dev.cele.igdownloader.activities

import android.Manifest
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.ads.*
import dev.cele.igdownloader.R
import kotlinx.coroutines.*
import java.io.File


fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

const val REQUEST_EXTERNAL_STORAGE = 42


class MainActivity : AppCompatActivity() {
    private lateinit var urlText: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var preview: ImageView

    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlText = findViewById(R.id.urlText)
        progressBar = findViewById(R.id.progressBar)
        preview = findViewById(R.id.preview)

        findViewById<Button>(R.id.downloadButton).setOnClickListener { this.download() }

        hasWriteStoragePermission()

        MobileAds.initialize(this) { }
        adView = findViewById(R.id.adView)

        adView.adListener = object: AdListener(){

        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(hasFocus){
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            val clipText = with(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager){
                this.primaryClip?.getItemAt(0)?.text.toString()
            }

            if(clipText != urlText.text.toString() && Instagram.getPostID(clipText) != null){
                urlText.text = clipText.toEditable()
                download(clipText)
            }
        }
    }

    private fun download(inputUrl: String? = null) {
        val url = inputUrl ?: urlText.text.toString()
        progressBar.progress = 0

        if(!hasWriteStoragePermission()) return

        CoroutineScope(Dispatchers.IO).launch {

            val postID = Instagram.getPostID(url) ?: return@launch
            val downloadUrls = Instagram.getDownloadUrls(postID) ?: return@launch

            downloadUrls.forEach { downloadUrl ->

                val downloadFile = File(applicationContext.cacheDir, Instagram.extractFileName(downloadUrl))
                val downloadPath = downloadFile.absolutePath

                val downloaded = HttpClient.download(downloadUrl, downloadPath){ current, max ->
                    progressBar.progress = (current * 100 / max).toInt()
                }

                if(downloaded){
                    //adding the image to the MediaStore (so it will be visible in other apps)
                    runCatching{
                        Log.d("MEDIASTORESAVE", MediaStore.Images.Media.insertImage(contentResolver, downloadFile.absolutePath, downloadFile.name, "Image Description"))
                    }

                    //setting the preview image
                    val options = BitmapFactory.Options().apply { /*inSampleSize = 2*/ }
                    val b = BitmapFactory.decodeFile(downloadPath, options)
                    withContext(Dispatchers.Main) {
                        preview.setImageBitmap(b)
                    }

                    //deleting it from the cache
                    val deleted = downloadFile.delete()
                    Log.d("DELETED", deleted.toString())
                }

                withContext(Dispatchers.Main){
                    Toast.makeText(applicationContext, "Download: " + if(downloaded) "OK!" else "FAILED!", Toast.LENGTH_SHORT).show()
                }
            }
        }




    }

    private fun hasWriteStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)

            return false
        }

        return true
    }

}