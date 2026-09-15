# Consumer ProGuard rules for :shared:network
-keep class com.identityx.android.core.network.model.** { *; }
-keep class com.identityx.android.core.network.session.SessionManager { *; }
-keep class com.identityx.android.core.network.session.SessionEventBus { *; }
-keep interface com.identityx.android.core.network.industrial.NetworkResponse { *; }
