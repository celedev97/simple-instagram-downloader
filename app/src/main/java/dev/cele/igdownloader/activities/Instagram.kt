package dev.cele.igdownloader.activities

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import java.io.File
import java.lang.Exception


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

    fun extractFileName(fileUrl:String) = extractFileNameRegex.findAll(fileUrl).mapNotNull { it.groups[1]?.value }.first()
    fun validateUrl(text:String) = checkUrlRegex.matches(text)
    suspend fun getDownloadUrl(instagramUrl: String): String?{
        try{
            val request: Request = Request.Builder().url("https://api.instagram.com/oembed/?url=$instagramUrl").build()

            val call = HttpClient.client.newCall(request)
            val response = call.execute()

            if(response.body != null && response.code == 200) {
                val oembedResult = Gson().fromJson(response.body!!.string(), OembedResult::class.java)
                return oembedResult.thumbnail_url
            }
        }catch (ex : Exception){
            Log.e("ERROR", ex.message.toString())
        }
        return null
    }

}