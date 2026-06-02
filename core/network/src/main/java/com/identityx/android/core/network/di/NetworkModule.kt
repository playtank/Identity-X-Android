package com.identityx.android.core.network.di

import com.apollographql.apollo3.ApolloClient
import com.identityx.android.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 绑定到全局 App 生命周期
object NetworkModule {

    private val BASE_URL = BuildConfig.BASE_URL

    /**
     * 向全工程提供工业级 Ktor HttpClient
     */
    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { prettyPrint = true; ignoreUnknownKeys = true })
            }

            defaultRequest {
                url(BASE_URL)
            }

//            install(Auth) {
//                bearer {
//                    loadTokens {
//                        // 🌟 DI 的威力：这里未来可以直接注入你的 TokenStorage 依赖，而不是硬编码！
//                        BearerTokens("mock_access_token", "mock_refresh_token")
//                    }
//
//                    refreshTokens {
//                        val refreshClient = HttpClient(CIO) {
//                            install(ContentNegotiation) { json() }
//                        }
//                        try {
//                            val response: AuthResponse = refreshClient.post("$BASE_URL/api/v1/auth/refresh") {
//                                contentType(ContentType.Application.Json)
//                                setBody(mapOf("refreshToken" to oldTokens?.refreshToken))
//                            }.body()
//
//                            BearerTokens(response.accessToken, response.refreshToken)
//                        } catch (e: Exception) {
//                            null
//                        }
//                    }
//
//                    sendWithoutRequest { request ->
//                        request.url.encodedPath.endsWith("/auth/login") ||
//                                request.url.encodedPath.endsWith("/auth/refresh")
//                    }
//                }
//            }
        }
    }

    /**
     * 向全工程提供 Apollo GraphQL 客户端
     */
    @Provides
    @Singleton
    fun provideApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("$BASE_URL/graphql")
            .build()
    }
}