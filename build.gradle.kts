plugins {
    kotlin("jvm") version "2.4.20"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.mk7a"
version = "2.0.0"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.26-alpha")
    implementation("org.bstats:bstats-bukkit:3.2.1")

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:26.3.build.26-alpha")
}

tasks.test {
    useJUnitPlatform()
}

// Keeps the unshaded jar from overwriting the plugin jar, which shares its name otherwise
tasks.jar {
    archiveClassifier.set("plain")
}

tasks.shadowJar {
    relocate("org.bstats", "com.mk7a.soulbind.bstats")
    minimize()
    archiveClassifier.set("")
    // Let shadow's Kotlin module transformer see every module file rather than dropping duplicates first
    filesMatching("META-INF/*.kotlin_module") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val properties = mapOf("version" to project.version)
    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}

tasks.runServer {
    minecraftVersion("26.3")
}
