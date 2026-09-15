# Consumer ProGuard rules for :shared:auth-domain
# Keep domain model sealed interfaces and their subclasses so R8 in consuming
# modules doesn't strip them during shrinking.
-keep class com.identityx.auth_domain.domain.model.** { *; }
-keep interface com.identityx.auth_domain.data.LoginDataSource { *; }
