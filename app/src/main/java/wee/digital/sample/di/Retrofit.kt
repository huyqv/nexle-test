package wee.digital.sample.di

import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import wee.digital.sample.BuildConfig
import java.util.concurrent.TimeUnit

val retrofitModule = module {
    single { Cache(androidApplication().cacheDir, 10L * 1024 * 1024) }
    single { GsonBuilder().create() }
    single { httpClient }
    single { retrofit }
}

private val loggingInterceptor: Interceptor
    get() = HttpLoggingInterceptor().also {
        it.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.HEADERS
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

private val Scope.httpClient: OkHttpClient
    get() = OkHttpClient.Builder().apply {
        cache(get())
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(15, TimeUnit.SECONDS)
        writeTimeout(15, TimeUnit.SECONDS)
        retryOnConnectionFailure(true)
        addInterceptor(loggingInterceptor)
    }.build()

private val Scope.retrofit: Retrofit
    get() = Retrofit.Builder()
        .baseUrl("http://streaming.nexlesoft.com:4000/api/")
        .addConverterFactory(GsonConverterFactory.create()).client(get())
        .build()