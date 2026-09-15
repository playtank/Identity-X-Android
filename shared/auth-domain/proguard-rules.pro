# :shared:auth-domain ProGuard rules
# Keep domain model sealed interfaces and their subclasses so R8 doesn't strip
# them when the consuming module shrinks.
-keep class com.identityx.auth_domain.domain.model.** { *; }
-keep interface com.identityx.auth_domain.data.LoginDataSource { *; }
