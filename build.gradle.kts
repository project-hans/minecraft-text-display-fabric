//plugins {
//    kotlin("jvm") version "2.1.0"
//}
//
//group = "com.heledron"
//version = "1.0-SNAPSHOT"
////application {
////    mainClass.set("com.heledron.TextDisplayExperiments") // Add the appropriate main class if necessary
////}
//
//repositories {
//    mavenCentral()
//    maven("https://repo.papermc.io/repository/maven-public/")
//    maven("https://oss.sonatype.org/content/groups/public/")
//    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
//}
//
//dependencies {
//    implementation(kotlin("stdlib-jdk8", "2.1.0"))
//    implementation("io.papermc.paper", "paper-api", "1.21.4-R0.1-SNAPSHOT")
//    implementation("org.spigotmc", "spigot-api", "1.21.3-R0.1-SNAPSHOT")
//    // Uncomment this if you need to add Kotlin test dependency
//    // testImplementation(kotlin("test", "2.1.0"))
//}
//
//tasks.withType<JavaCompile> {
////    options.compilerArgs = listOf(
////        "-Djogl.disable.openglcore=true",
////        "-Djogl.disable.opengles=true",
////        "-Djogl.disable.gpu=true"
////    )
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//
//}
//
//tasks.jar {
//    manifest {
//        attributes["Implementation-Title"] = "TextDisplayExperiments"
//        attributes["Implementation-Version"] = version
//    }
//}
//
//tasks.register<Jar>("shadeJar") {
//    from(configurations.runtimeClasspath.get().filter { it.exists() })
//    with(tasks.jar.get())
//    archiveClassifier.set("shaded")
//}

import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar

plugins {
    kotlin("jvm") version "2.0.20-Beta1"
    kotlin("kapt") version "2.0.20-Beta1"
    id("com.gradleup.shadow") version "9.0.0-beta8"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    kotlin("plugin.serialization") version "2.1.10"

}

group = "io.kouna"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigot-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        name = "sonatype-snapshots"
    }
    maven("https://oss.sonatype.org/content/repositories/central") {
        name = "sonatype-central"
    }
}

dependencies {
//    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT") // The Spigot API with no shadowing. Requires the OSS repo.
//    compileOnly("org.spigotmc:spigot:1.21.4-R0.1-SNAPSHOT") // The full Spigot server with no shadowing. Requires mavenLocal.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
//    compileOnly("io.papermc.paper:paper:1.21.4-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("me.friwi", "jcefmaven", "127.3.1")
    implementation("org.joml", "joml", "1.10.8")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)

    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"

    }
}
kotlin {
    jvmToolchain(21)
}
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


//completely useless, but will complain if missing
val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")

val generateTemplates by tasks.registering(Copy::class) {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets["main"].java.srcDir(generateTemplates.map { it.destinationDir })