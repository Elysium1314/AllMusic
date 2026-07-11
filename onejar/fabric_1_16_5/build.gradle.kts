plugins {
    id("fabric-loom") version Versions.fabricLoom
}

// 直接引用 client 和 server 模块的源码目录，无需手动复制
val clientSourceDir = project(":client:fabric_1_16_5").projectDir.resolve("src/main/java")
val serverSourceDir = project(":server:fabric_1_16_5").projectDir.resolve("src/main/java")
val clientResDir = project(":client:fabric_1_16_5").projectDir.resolve("src/main/resources")
val serverResDir = project(":server:fabric_1_16_5").projectDir.resolve("src/main/resources")

sourceSets {
    main {
        java {
            setSrcDirs(listOf(clientSourceDir, serverSourceDir))
        }
        resources {
            setSrcDirs(listOf(
                file("src/main/resources"),  // onejar 自己的合并资源（fabric.mod.json）
                clientResDir,
                serverResDir
            ))
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.16.5")
    mappings(loom.officialMojangMappings())
    modCompileOnly("net.fabricmc:fabric-loader:0.18.5")

    modCompileOnly("net.fabricmc.fabric-api:fabric-api:0.42.0+1.16")

    modImplementation(include("net.kyori:examination-api:1.3.0")!!)
    modImplementation(include("net.kyori:examination-string:1.3.0")!!)
    modImplementation(include("net.kyori:adventure-platform-api:4.0.0")!!)
    modImplementation(include("net.kyori:adventure-text-serializer-gson:4.9.3")!!)
    modImplementation(include("net.kyori:adventure-text-serializer-legacy:4.9.3")!!)
    modImplementation(include("net.kyori:adventure-text-serializer-plain:4.9.3")!!)
    modImplementation(include("net.kyori:adventure-text-minimessage:4.26.1")!!)
    modImplementation(include("net.kyori:adventure-api:4.26.1")!!)
    modImplementation(include("net.kyori:adventure-key:4.26.1")!!)
}

tasks {
    processResources {
        // onejar 自己的 fabric.mod.json 优先（排在 srcDirs 第一位），
        // client/server 的 fabric.mod.json 作为重复项被排除
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version
            )
        }
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)

        archiveFileName.set("[fabric-1.16.5]AllMusic-${project.version}.jar")
        destinationDirectory.set(file("${parent!!.projectDir}/../build"))
    }

    build {
        dependsOn(remapJar)
    }
}
