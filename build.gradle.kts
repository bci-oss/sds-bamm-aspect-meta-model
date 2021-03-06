import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask

plugins {
    id("com.github.node-gradle.node") version "2.2.4"
    id ("java-library")
    id ("maven-publish")
    id ("signing")
}

group = "io.openmanufacturing"
version = "1.0.0"

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation ("org.junit.jupiter:junit-jupiter:5.6.3")
    testImplementation ("org.assertj:assertj-core:3.18.1")
    testImplementation ("org.topbraid:shacl:1.3.1")
    testImplementation ("io.vavr:vavr:0.10.3")
}

publishing {
    publications {
        create<MavenPublication>("mavenRelease") {
            groupId = "io.openmanufacturing"
            artifactId = "sds-aspect-meta-model"
            version = "1.0.0"

            from(components["java"])

            pom {
                name.set("BAMM Aspect Meta Model")
                licenses {
                    license {
                        name.set("Mozilla Public License, Version 2.0")
                        url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                    }
                }
            }
        }
    }
}

signing {
    var signingKey : String? = System.getenv("PGP_KEY")
    var signingPassword : String? = System.getenv("PGP_KEY_PASSWORD")
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications["mavenRelease"])
}

configure<NodeExtension> {
    setVersion("12.13.1")
    setNpmVersion("6.9.0")
    setDownload(true)
    setWorkDir(file("${project.buildDir}/node"))
    setNpmWorkDir(file("${project.buildDir}/npm"))
    setNodeModulesDir(file("${project.buildDir}"))
}

val downloadAntoraCli = tasks.register<NpmTask>("downloadAntoraCli") {
    setArgs(listOf("install", "@antora/cli"))
    onlyIf { !file("${project.buildDir}/node_modules/@antora/cli/bin/antora").exists() }
}

val downloadAntoraSiteGeneratorLunr = tasks.register<NpmTask>("downloadAntoraSiteGeneratorLunr") {
    setArgs(listOf("install", "antora-site-generator-lunr"))
    onlyIf { !file("${project.buildDir}/node_modules/antora-site-generator-lunr/lib/index.js").exists() }
}

val downloadPlantUml = tasks.register<NpmTask>("downloadPlantUml") {
    setArgs(listOf("install", "asciidoctor-plantuml"))
    onlyIf { !file("${project.buildDir}/node_modules/asciidoctor-plantuml/package.json").exists() }
}

tasks.register<NodeTask>("antora") {
    setDependsOn(listOf(downloadAntoraCli, downloadAntoraSiteGeneratorLunr, downloadPlantUml))
    setScript(file("${project.buildDir}/node_modules/@antora/cli/bin/antora"))
    setArgs(listOf("--generator", "antora-site-generator-lunr", "site.yml", "--stacktrace"))
    setWorkingDir(project.projectDir)
}

tasks.register<NodeTask>("antoraLocal") {
    setDependsOn(listOf(downloadAntoraCli, downloadAntoraSiteGeneratorLunr, downloadPlantUml))
    setScript(file("${project.buildDir}/node_modules/@antora/cli/bin/antora"))
    setArgs(listOf("--generator", "antora-site-generator-lunr", "site-local.yml", "--stacktrace"))
    setWorkingDir(project.projectDir)
}
