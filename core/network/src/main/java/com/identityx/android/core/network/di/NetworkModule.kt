package com.identityx.android.core.network.di

import com.identityx.android.core.network.BuildConfig
import com.identityx.android.core.network.restful.IdentityXApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.ANDROID
                level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            }
            engine {
                connectTimeout = 15_000
                socketTimeout  = 30_000
            }
        }
    }

    @Provides
    @Singleton
    fun provideIdentityXApiClient(client: HttpClient): IdentityXApiClient {
        return IdentityXApiClient(client, BuildConfig.BASE_URL)
    }
}
