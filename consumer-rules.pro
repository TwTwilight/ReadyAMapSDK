-keep class com.amap.api.** { *; }
-keep class com.autonavi.** { *; }
-keep class com.loc.** { *; }

-keep class com.amap.api.col.** { *; }
-keepclassmembers class com.amap.api.col.** { *; }

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepclasseswithmembernames class * {
    native <methods>;
}

-dontwarn com.amap.api.**
-dontwarn com.autonavi.**
-dontwarn org.apache.**
-dontwarn javax.**
