package com.identityx.android.core.network.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.identityx.android.core.network.graphql.generated.PingQuery
import javax.inject.Inject

class IdentityXGraphService @Inject constructor(
    private val apolloClient: ApolloClient
) {
    suspend fun ping(): ApolloResponse<PingQuery.Data> =
        apolloClient.query(PingQuery()).execute()
}
