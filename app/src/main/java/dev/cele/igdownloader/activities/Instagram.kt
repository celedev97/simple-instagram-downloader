package dev.cele.igdownloader.activities

import android.util.Log
import com.google.gson.JsonParser
import okhttp3.*


object Instagram {
    private val checkUrlRegex = Regex("https?://(?:www\\.)?(?:instagram\\.com|instagr\\.am)/p/(\\w+).+", RegexOption.IGNORE_CASE)
    private val extractFileNameRegex = Regex("/([\\w\\d_.]+\\.[a-z]{3}).*", RegexOption.IGNORE_CASE)

    fun extractFileName(fileUrl:String) = extractFileNameRegex.findAll(fileUrl).mapNotNull { it.groups[1]?.value }.first()
    fun getPostID(igUrl:String) : String? = checkUrlRegex.findAll(igUrl).mapNotNull { it.groups[1]?.value }.firstOrNull()
    suspend fun getDownloadUrls(postID: String): List<String>{
        val output = mutableListOf<String>()
        try{
            val request: Request = Request.Builder().url("https://www.instagram.com/p/$postID/?__a=1").build()

            val call = HttpClient.client.newCall(request)
            val response = call.execute()

            if(response.body != null && response.code == 200) {
                val bodyContent = response.body!!.string()
                val json = JsonParser.parseString(bodyContent).asJsonObject

                output.addAll( json
                    .getAsJsonObject("graphql")
                    .getAsJsonObject("shortcode_media")
                    .getAsJsonObject("edge_sidecar_to_children")
                    .getAsJsonArray("edges").map {
                        it.asJsonObject
                            .getAsJsonObject("node")
                            .get("display_url").asString
                    }
                )
            }
        }catch (ex : Exception){
            Log.e("ERROR", ex.message.toString())
        }
        return output
    }

}