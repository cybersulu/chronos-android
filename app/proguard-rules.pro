# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line numbers with compact SourceFile attribute for crash diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep ViewModel constructor invoked via reflection by ViewModelProvider
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}

# Preserve enum names persisted in SharedPreferences
-keep enum com.example.model.CountdownSortOption { *; }

# Strip debug/verbose logging from release DEX
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

