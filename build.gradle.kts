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
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${project.property("minecraft_version")}-SNAPSHOT")
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

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 21
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

loom {
    accessWidenerPath = file("src/main/resources/higtools.accesswidener")
}
