# Proguard rules for Personal AI Life OS
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class com.personal.lifeos.data.remote.** { *; }
-keep class com.personal.lifeos.data.local.entity.** { *; }
