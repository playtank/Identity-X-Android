package com.identityx.authentication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.identityx.android.core.network.session.SessionEventBus
import com.identityx.authentication.navigation.AppNavHost
import com.identityx.authentication.ui.theme.AuthenticationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthenticationTheme {
                AppNavHost(sessionEventBus = sessionEventBus)
            }
        }
    }
}
