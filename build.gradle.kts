plugins {
    id("java")
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadowJar)
}

group = "com.spanner"
version = "1.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(libs.minestom);
    implementation(libs.minestomce);
    implementation(libs.bundles.logging)
    implementation(libs.bundles.terminal)
}

tasks {
    blossom {
        replaceToken("&VERSION", version)
    }
    shadowJar {
        manifest {
            attributes("Main-Class" to "com.spanner.cestom.Server")
        }
        archiveBaseName.set("cestom")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}
