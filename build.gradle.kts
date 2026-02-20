plugins {
    java
    application
}

group = "com.example"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.Version_17
    targetCompatibility = JavaVersion.Version_17
}

application {
    mainClass.set("DataClassifierApp")
}

repositories {
    mavenCentral()
}