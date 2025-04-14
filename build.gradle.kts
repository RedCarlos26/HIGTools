plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
}

base {
    archivesName = project.property("archives_base_name").toString()
    version = project.property("mod_version").toString()
    group = project.property("maven_group").toString()
}

repositories {
    maven("https://maven.meteordev.org/releases") {
        name = "meteor-maven"
    }

    maven("https://maven.meteordev.org/snapshots") {
        name = "meteor-maven-snapshots"
    }
    mavenCentral()
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${project.property("minecraft_version")}-SNAPSHOT")
}

loom {
    accessWidenerPath = file("src/main/resources/higtools.accesswidener")
}

tasks {
    processResources {
        val propertiesMap = mapOf(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version")
        )

        inputs.properties(propertiesMap)
        filesMatching("fabric.mod.json") {
            expand(propertiesMap)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    withType<JavaCompile> {
        options.release = 21
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }

    javadoc {
        with(options as StandardJavadocDocletOptions) {
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("encoding", "UTF-8")
            addStringOption("charSet", "UTF-8")
        }
    }
}
