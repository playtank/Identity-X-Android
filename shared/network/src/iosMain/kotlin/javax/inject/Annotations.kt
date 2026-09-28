// No-op JSR-330 annotation stubs for iOS/Native targets.
// On Android/JVM these come from javax.inject:javax.inject:1 and are
// processed by Hilt/Dagger. On iOS no DI container is present — the
// annotations are retained in source so commonMain code compiles
// unchanged, but they have no runtime effect.
package javax.inject

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Singleton

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Named(val value: String = "")
