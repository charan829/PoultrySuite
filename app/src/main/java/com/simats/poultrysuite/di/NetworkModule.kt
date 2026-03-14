package com.simats.poultrysuite.di

import com.simats.poultrysuite.data.remote.PoultryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Uses `adb reverse tcp:3000 tcp:3000` — works for USB-connected physical devices and emulators
    private const val BASE_URL = "http://localhost:3000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionManager: com.simats.poultrysuite.data.local.SessionManager): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            android.util.Log.d("POULTRY_API_LOG", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = runBlocking {
                sessionManager.authToken.first()
            }
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePoultryApi(retrofit: Retrofit): PoultryApi {
        return retrofit.create(PoultryApi::class.java)
    }
}
