package dev.cele.igdownloader.activities

import android.Manifest
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.ads.*
import dev.cele.igdownloader.R


fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

val REQUEST_EXTERNAL_STORAGE = 1


class MainActivity : AppCompatActivity() {
    lateinit var urlText: EditText
    lateinit var progressBar: ProgressBar
    lateinit var preview: ImageView

    lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlText = findViewById(R.id.urlText)
        progressBar = findViewById(R.id.progressBar)
        preview = findViewById(R.id.preview)

        findViewById<Button>(R.id.downloadButton).setOnClickListener { this.download() }

        hasWriteStoragePermission()

        MobileAds.initialize(this) { }
        adView = findViewById<AdView>(R.id.adView)

        adView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("ADS", "onAdLoaded")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("ADS", "FAILED $p0")
            }

            override fun onAdOpened() {
                super.onAdOpened()
                Log.d("ADS", "onAdOpened")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d("ADS", "onAdClicked")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d("ADS", "onAdClosed")
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(hasFocus){
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            val clipText = with(getSystemService(CLIPBOARD_SERVICE) as ClipboardManager){
                this.primaryClip?.getItemAt(0)?.text.toString()
            }

            if(clipText != urlText.text.toString() && Instagram.validateUrl(clipText)){
                urlText.text = clipText.toEditable()
                download(clipText)
            }
        }
    }

    private fun download(inputUrl: String? = null) {
        val url = inputUrl ?: urlText.text.toString()
        progressBar.progress = 0

        if(!Instagram.validateUrl(url)) return
        if(!hasWriteStoragePermission()) return

        val downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath

        Instagram.getDownloadUrl(url){ downloadUrl ->
            Fuel.download()
            if(downloadUrl != null){
                Instagram.download(downloadUrl, downloadDirectory,
                    progress = {progressBar.progress = it.toInt(); Log.d("PROGRESS", it.toString())},
                    done = {
                        Toast.makeText(this, "DOWNLOAD COMPLETE!", Toast.LENGTH_SHORT).show()
                        if(it != null)
                            preview.setImageBitmap(BitmapFactory.decodeFile(it.absolutePath))
                    }
                )

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