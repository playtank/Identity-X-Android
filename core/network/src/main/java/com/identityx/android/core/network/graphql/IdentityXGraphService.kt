package com.identityx.android.core.network.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.identityx.android.core.network.graphql.generated.PingQuery
import javax.inject.Inject

/**
 * GraphQL client service for Identity X.
 *
 * Not used yet — wired up and ready to use when the backend exposes a GraphQL endpoint.
 * Add real query/mutation methods here as the schema grows.
 *
 * Endpoint: <BASE_URL>/graphql
 */
class IdentityXGraphService @Inject constructor(
    private val apolloClient: ApolloClient
) {
    /**
     * Placeholder health-check query.
     * Replace with real operations once the schema is defined.
     */
    suspend fun ping(): ApolloResponse<PingQuery.Data> =
        apolloClient.query(PingQuery()).execute()
}
