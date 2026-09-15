# Consumer ProGuard rules for :shared:local
-keep interface com.identityx.local.domain.TokenProvider { *; }
-keep interface com.identityx.local.domain.UserPreferencesProvider { *; }
-keep class com.identityx.local.AppDatabase { *; }
