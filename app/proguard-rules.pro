# ExtenderPass ProGuard Rules
# Keep crypto classes intact — essential for deterministic output
-keep class javax.crypto.** { *; }
-keep class javax.crypto.spec.** { *; }
-keep class java.security.** { *; }

# Keep PasswordGenerator constants so they are never obfuscated
-keep class tadakai.extenderpass.core.PasswordGenerator { *; }

# Compose
-keep class androidx.compose.** { *; }
