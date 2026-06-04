package com.identityx.android.core.network.di

import com.identityx.android.core.network.BuildConfig
import com.identityx.android.core.network.restful.IdentityXApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Shared OkHttpClient — used by Retrofit (and Apollo once re-enabled).
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideIdentityXApiService(retrofit: Retrofit): IdentityXApiService {
        return retrofit.create(IdentityXApiService::class.java)
    }

    // Apollo GraphQL — temporarily disabled pending Apollo Gradle plugin AGP 9 support
    // Re-enable once com.apollographql.apollo plugin supports AGP 9 built-in Kotlin
    // @Provides
    // @Singleton
    // fun provideApolloClient(okHttpClient: OkHttpClient): ApolloClient {
    //     return ApolloClient.Builder()
    //         .serverUrl("${BuildConfig.BASE_URL}/graphql")
    //         .okHttpClient(okHttpClient)
    //         .build()
    // }
}
