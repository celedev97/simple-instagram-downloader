package dev.cele.igdownloader.activities

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.await
import com.github.kittinunf.fuel.gson.responseObject
import com.google.gson.annotations.SerializedName
import java.io.File

data class OembedResult (
    @SerializedName("version") val version : Double,
    @SerializedName("title") val title : String,
    @SerializedName("author_name") val author_name : String,
    @SerializedName("author_url") val author_url : String,
    @SerializedName("author_id") val author_id : String,
    @SerializedName("media_id") val media_id : String,
    @SerializedName("provider_name") val provider_name : String,
    @SerializedName("provider_url") val provider_url : String,
    @SerializedName("type") val type : String,
    @SerializedName("width") val width : Int,
    @SerializedName("height") val height : String,
    @SerializedName("html") val html : String,
    @SerializedName("thumbnail_url") val thumbnail_url : String,
    @SerializedName("thumbnail_width") val thumbnail_width : Int,
    @SerializedName("thumbnail_height") val thumbnail_height : Int
)


object Instagram {
    private val checkUrlRegex = Regex("https?://(?:www\\.)?(?:instagram\\.com|instagr\\.am)/.+", RegexOption.IGNORE_CASE)
    private val extractFileNameRegex = Regex("/([\\w\\d_]+\\.[a-z]{3})[^\\w\\d]*.*\$", RegexOption.IGNORE_CASE)

    fun validateUrl(text:String) = checkUrlRegex.matches(text)
    fun getDownloadUrl(instagramUrl: String, callback: (String?)->Unit){
        Fuel.get().resp
        Fuel.get("https://api.instagram.com/oembed/?url=$instagramUrl").responseObject<OembedResult>{ _, _, (data, error) ->
            callback(if(error == null) data!!.thumbnail_url else null)
        }
    }

    fun download(fileUrl: String, downloadDirectory: String, progress: (Float) -> Unit = {}, done: (File?) -> Unit = {}){
        val fileName = extractFileNameRegex.findAll(fileUrl).mapNotNull { it.groups[1]?.value }.first()
        Log.d("EXTRACTED", fileName)

        val outputFile = File(downloadDirectory, fileName)

        Fuel.download(fileUrl)
            .fileDestination { _, _ -> outputFile }
            .progress { readBytes, totalBytes ->
                progress(readBytes.toFloat() / totalBytes.toFloat() * 100)
            }
            .response { _, _, (_, error) ->
                Log.d("PROGRESS DLEND", error.toString())
                done(if(error == null) outputFile else null)
            }

    }

}