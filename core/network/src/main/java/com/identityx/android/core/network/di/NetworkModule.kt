package com.identityx.android.core.network.di

import android.content.Context
import android.net.ConnectivityManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import com.identityx.android.core.network.shell.BuildConfig
import com.identityx.android.core.network.NetworkMonitor
import com.identityx.android.core.network.graphql.IdentityXGraphService
import com.identityx.android.core.network.industrial.IndustrialKtorApi
import com.identityx.android.core.network.industrial.MockIndustrialClientProvider
import com.identityx.android.core.network.model.AuthResponse
import com.identityx.android.core.network.restful.IdentityXApiClient
import com.identityx.android.core.network.session.SessionManager
import com.identityx.local.domain.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IdentityHttpClient

// Used only for the token refresh call inside the Auth plugin
@Serializable
private data class RefreshBody(val refreshToken: String)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    @IdentityHttpClient
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.ENABLE_NETWORK_LOGGING)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    @Singleton
    fun provideHttpClient(
        @IdentityHttpClient okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String,
        tokenProvider: TokenProvider,
        sessionManager: SessionManager,
        networkMonitor: NetworkMonitor,
    ): HttpClient {
        val client = HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }

            install(HttpTimeout) {
                connectTimeoutMillis = 4_000
                requestTimeoutMillis = 5_000
                socketTimeoutMillis  = 5_000
            }

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Logging) {
                logger = Logger.ANDROID
                level  = if (BuildConfig.ENABLE_NETWORK_LOGGING) LogLevel.BODY else LogLevel.NONE
            }

            install(Auth) {
                bearer {
                    // Only attach the Bearer token to requests that actually need it.
                    // Auth endpoints are public — sending an old/expired token there
                    // causes a spurious refresh attempt that logs the user out.
                    sendWithoutRequest { request ->
                        val url = request.url.toString()
                        !url.contains("/api/v1/auth/login") &&
                        !url.contains("/api/v1/auth/refresh") &&
                        !url.contains("/api/v1/auth/logout")
                    }

                    loadTokens {
                        val access  = tokenProvider.getAccessToken()  ?: return@loadTokens null
                        val refresh = tokenProvider.getRefreshToken() ?: return@loadTokens null
                        BearerTokens(accessToken = access, refreshToken = refresh)
                    }

                    refreshTokens {
                        val currentRefresh = tokenProvider.getRefreshToken()
                        if (currentRefresh == null) {
                            sessionManager.onSessionExpired()
                            return@refreshTokens null
                        }
                        try {
                            val response: AuthResponse =
                                client.post("$baseUrl/api/v1/auth/refresh") {
                                    markAsRefreshTokenRequest()
                                    contentType(ContentType.Application.Json)
                                    setBody(RefreshBody(refreshToken = currentRefresh))
                                }.body()
                            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: Exception) {
                            sessionManager.onSessionExpired()
                            null
                        }
                    }
                }
            }
        }

        client.plugin(HttpSend).intercept { request ->
            if (!networkMonitor.isCurrentlyConnected())
                throw IOException("Network is completely unavailable.")
            execute(request)
        }

        return client
    }

    @Provides
    @Singleton
    fun provideIdentityXApiClient(
        httpClient: HttpClient,
        @Named("baseUrl") baseUrl: String
    ): IdentityXApiClient = IdentityXApiClient(httpClient, baseUrl)

    @Provides
    @Singleton
    fun provideApolloClient(
        @IdentityHttpClient okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String
    ): ApolloClient = ApolloClient.Builder()
        .serverUrl("$baseUrl/graphql")
        .okHttpClient(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideIdentityXGraphService(
        apolloClient: ApolloClient
    ): IdentityXGraphService = IdentityXGraphService(apolloClient)

    @Provides
    @Named("industrialBaseUrl")
    fun provideIndustrialBaseUrl(): String = "https://edge-ingestion.yourplant.com"

    @Provides
    @Named("industrialHttpClient")
    @Singleton
    fun provideIndustrialHttpClient(): HttpClient = MockIndustrialClientProvider.create()

    @Provides
    @Singleton
    fun provideIndustrialKtorApi(
        @Named("industrialHttpClient") httpClient: HttpClient,
        @Named("industrialBaseUrl") baseUrl: String
    ): IndustrialKtorApi = IndustrialKtorApi(httpClient, baseUrl)
}
