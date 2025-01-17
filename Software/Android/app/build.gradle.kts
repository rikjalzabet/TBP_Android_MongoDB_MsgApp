plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "hr.foi.msgappmongodb"
    compileSdk = 35

    defaultConfig {
        applicationId = "hr.foi.msgappmongodb"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        //isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            //excludes += "META-INF/native-image/native-image.properties"
            excludes += "META-INF/native-image/native-image.properties"
            excludes+="META-INF/native-image/reflect-config.json"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

   /* implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.mongodb:mongodb-driver-core:5.2.1")
    implementation("org.mongodb:bson:5.2.1")
    implementation("org.mongodb:bson-kotlinx:5.2.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
*/
    //implementation ("org.mongodb:mongodb-driver-core:5.2.1")
    //implementation ("org.mongodb:bson:5.2.1")
    //implementation("org.mongodb:mongodb-driver-sync:5.2.1")
   // implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.2.1")
    implementation("org.mongodb:bson-kotlinx:5.2.1")
    //implementation ("dnsjava:dnsjava:3.5.2")
    //implementation ("org.mongodb:mongodb-driver-android:5.2.1")
    //implementation("javax.naming:jndi:1.2.1")
    //implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    //implementation("org.eclipse.jetty:jetty-jndi:12.0.16")

    //implementation("simple-jndi:simple-jndi:0.11.4.1")
   // implementation("org.eclipse.jetty:jetty-jndi:12.0.16")

    //implementation ("org.litote.kmongo:kmongo-async:5.2.1")



    //implementation ("com.sun.jndi:dns:3.5.2")
    //implementation ("javax.naming:javax.naming-api:1.3.5")

    //implementation ("org.mongodb:mongodb-driver-sync:4.10.1")
    //implementation ("org.mongodb:mongodb-driver-android:4.10.1")
}
