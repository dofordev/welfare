package com.skt.welfare.api

import com.skt.welfare.BuildConfig
import com.skt.welfare.Constants
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface TokTokApi {

    @GET("/service.pe")
    fun auth(
        @Query("primitive") primitive: String,
        @Query("companyCd") companyCd: String?,
        @Query("appId") appId: String?,
        @Query("appVer") appVer: String?,
        @Query("encPwd") encPwd: String?,
        @Query("osName") osName: String?,
        @Query("groupCd") groupCd: String?,
        @Query("lang") lang: String?,
        @Query("authKey") authKey: String?,
        @Query("osVersion") osVersion: Int?,
        @Query("mdn") mdn: String?

    ): Call<TokTokResponse>

    companion object {

        fun create(): TokTokApi {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            var url = Constants.toktokPrdUrl;
            if(BuildConfig.FLAVOR.equals("dev")){
                url = Constants.toktokDevUrl
            }

            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(TikXmlConverterFactory.create(TikXml.Builder().exceptionOnUnreadXml(false).build()))
                .build()
                .create(TokTokApi::class.java)
        }
    }
}

