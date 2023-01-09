plugins {
    java
    groovy
    id("org.springframework.boot") version "2.7.7"
    id("io.spring.dependency-management") version "1.1.0"
    id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "io.regretan"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val javaOperatorVersion by ext("4.2.0")
val mysqlConnectorVersion by ext("8.0.28")
val mybatisSpringStarterVersion by ext("2.3.0")
val testContainerVersion by ext("1.17.6")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:$mybatisSpringStarterVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.javaoperatorsdk:operator-framework:$javaOperatorVersion")

    runtimeOnly("mysql:mysql-connector-java:$mysqlConnectorVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("io.javaoperatorsdk:operator-framework:$javaOperatorVersion")
    annotationProcessor("io.fabric8:crd-generator-apt:6.3.1")

    testRuntimeOnly("mysql:mysql-connector-java")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
    testImplementation("org.testcontainers:mysql:$testContainerVersion")
}

apply(from = "${rootDir}/gradle/jib.gradle")
