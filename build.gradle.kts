plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.compose") version "2.3.0"
    id("org.jetbrains.compose") version "1.11.0-beta03"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "edu.teamcandy"
version = "1.0-SNAPSHOT"
val ktorVersion = "3.4.1"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.50.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.0")

    // Sqlite
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    //Ktor
    implementation("io.ktor:ktor-server-core-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-cors-jvm:${ktorVersion}")

    // Thymeleaf
    implementation("io.ktor:ktor-server-thymeleaf:${ktorVersion}")

    //Swagger
    implementation("io.ktor:ktor-server-swagger:${ktorVersion}")
    implementation("io.ktor:ktor-server-openapi:${ktorVersion}")

    // QR code generation
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.google.zxing:javase:3.5.2")

    // Compose Desktop
    implementation(compose.desktop.currentOs)
}

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Run the Ktor web server (customer side)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("edu.teamcandy.MainKt")
    standardInput = System.`in`
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

compose.desktop {
    application {
        mainClass = "edu.teamcandy.desktop.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}