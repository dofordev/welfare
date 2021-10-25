package com.skt.welfare.api

import com.skt.welfare.BuildConfig
import com.skt.welfare.Constants
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface BackendApi {

    @Multipart
    @POST
    fun postOcrImage(
        @Url url : String,
        @Part image: MultipartBody.Part
    ): Call<OcrResponse>

    @POST("/api/adm/getEncodedImage")
    fun postImage(
        @Body params: ImageRequest
    ): Call<ImageResponse>

    companion object {

        fun create(): BackendApi {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {

                val request = it.request()
                    .newBuilder()
                    .addHeader("withCredentials", "true")
                    .addHeader("Authorization", "Bearer ${Constants.token}")
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)

                .build()

            var backendUrl = Constants.backendPrdUrl
            if(BuildConfig.FLAVOR == "dev" || BuildConfig.FLAVOR == "qa"){
                backendUrl = Constants.backendDevUrl
            }
            else if(BuildConfig.FLAVOR == "stg"){
                backendUrl = Constants.backendStgUrl
            }
            return Retrofit.Builder()
                .baseUrl(backendUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApi::class.java)
        }
    }
}

