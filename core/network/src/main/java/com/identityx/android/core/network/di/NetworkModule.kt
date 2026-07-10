package com.identityx.android.core.network.di

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import com.identityx.android.core.network.BuildConfig
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
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IdentityHttpClient

// Internal request model used only for the token refresh call inside the Auth plugin
@Serializable
private data class RefreshBody(val refreshToken: String)

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Named("baseUrl")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    /**
     * Plain OkHttpClient — used only as the Ktor engine transport and by Apollo.
     * Auth logic has moved to Ktor's [Auth] plugin — no interceptors or authenticator here.
     */
    @Provides
    @Singleton
    @IdentityHttpClient
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    /**
     * Ktor HttpClient with:
     * - ContentNegotiation (kotlinx.serialization JSON)
     * - Logging (Android logcat, body-level in debug)
     * - Auth bearer plugin:
     *     loadTokens   → reads current access + refresh token from [TokenProvider]
     *     refreshTokens → calls /api/v1/auth/refresh, saves new tokens, signals forced
     *                     logout via [SessionManager] if refresh fails
     */
    @Provides
    @Singleton
    fun provideHttpClient(
        @IdentityHttpClient okHttpClient: OkHttpClient,
        @Named("baseUrl") baseUrl: String,
        tokenProvider: TokenProvider,
        sessionManager: SessionManager
    ): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }

            install(Logging) {
                logger = Logger.ANDROID
                level  = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            }

            install(Auth) {
                bearer {
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
                            val response: AuthResponse = client.post("$baseUrl/api/v1/auth/refresh") {
                                markAsRefreshTokenRequest()
                                contentType(ContentType.Application.Json)
                                setBody(RefreshBody(refreshToken = currentRefresh))
                            }.body()
                            tokenProvider.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(
                                accessToken  = response.accessToken,
                                refreshToken = response.refreshToken
                            )
                        } catch (e: Exception) {
                            sessionManager.onSessionExpired()
                            null
                        }
                    }
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideIdentityXApiClient(
        httpClient: HttpClient,
        @Named("baseUrl") baseUrl: String
    ): IdentityXApiClient = IdentityXApiClient(httpClient, baseUrl)

    /**
     * Apollo GraphQL client — reuses the same plain OkHttpClient for transport.
     * Not used yet — wire [IdentityXGraphService] into a repository when the schema is ready.
     */
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
    fun provideIdentityXGraphService(apolloClient: ApolloClient): IdentityXGraphService =
        IdentityXGraphService(apolloClient)

    // Industrial edge telemetry — mock client until real backend is available
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
