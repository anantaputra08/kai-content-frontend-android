package com.example.kai_content.repositories

import com.example.kai_content.api.ContentApi
import com.example.kai_content.models.content.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ContentRepository(private val contentApi: ContentApi) {

    suspend fun getContents(token: String): List<Content> = suspendCoroutine { continuation ->
        contentApi.getContents("Bearer $token").enqueue(object : Callback<List<Content>> {
            override fun onResponse(call: Call<List<Content>>, response: Response<List<Content>>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body() ?: emptyList())
                } else {
                    continuation.resumeWithException(Exception("Error fetching contents: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<List<Content>>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

//    suspend fun getRelatedVideos(token: String, contentId: String): List<Content> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = contentApi.getRelatedContents(token, contentId)
//                if (response.isSuccessful && response.body() != null) {
//                    response.body()?.data ?: emptyList()
//                } else {
//                    throw Exception("Failed to load related videos: ${response.message()}")
//                }
//            } catch (e: Exception) {
//                throw e
//            }
//        }
//    }

//    suspend fun getContent(token: String, id: String): Content = suspendCoroutine { continuation ->
//        contentApi.getContent("Bearer $token", id).enqueue(object : Callback<Content> {
//            override fun onResponse(call: Call<Content>, response: Response<Content>) {
//                if (response.isSuccessful && response.body() != null) {
//                    continuation.resume(response.body()!!)
//                } else {
//                    continuation.resumeWithException(Exception("Error fetching content: ${response.code()}"))
//                }
//            }
//
//            override fun onFailure(call: Call<Content>, t: Throwable) {
//                continuation.resumeWithException(t)
//            }
//        })
//    }
}
