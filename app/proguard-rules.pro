-optimizationpasses 5
-keepattributes SourceFile,LineNumberTable
-dontusemixedcaseclassnames
-dontpreverify
-optimizations !code/simplification/artithmetic,!field/*,!class/merging/*


-dontwarn org.slf4j.impl.StaticLoggerBinder

# no enum should be obfuscated
#-keepclassmembers enum * {
#    public *;
#}