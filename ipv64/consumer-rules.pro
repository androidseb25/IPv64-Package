# kotlinx.serialization ships its own R8 rules; keep the generated serializers of the public models.
-keep,includedescriptorclasses class de.sr.ipv64.**$$serializer { *; }
-keepclassmembers class de.sr.ipv64.** {
    *** Companion;
}
-keepclasseswithmembers class de.sr.ipv64.** {
    kotlinx.serialization.KSerializer serializer(...);
}
