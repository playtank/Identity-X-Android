package com.identityx.android.core.network.restful

import com.identityx.android.core.network.model.AuthRequest
import com.identityx.android.core.network.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface IdentityXApiService {

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: AuthRequest): AuthResponse
}
