/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.0.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    `maven-publish`
    application
    java

}



repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13.1")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.0-jre")
}

application {
    // Define the main class for the application.
    //mainClass.set("nutch.App")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.apache.nutch"
            artifactId = "nutch"
            version = "1.18"

            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://repository.apache.org/service/local/staging/deploy/maven2")
        }
    }
}

//TODO delete once Gradle build system is fully implemented
ant.importBuild("build.xml") {old ->"ant-${old}"}

// the normal classpath
val classpathCollection: FileCollection = layout.files(
    file(project.properties["build.classes"]),
    fileTree(mapOf("dir" to project.properties["build.lib.dir"], "include" to listOf("*.jar")))
)
val classpath: String = classpathCollection.asPath

// ant target "init" renamed to "init-nutch" to avoid gradle naming conflits
tasks.register<Copy>("init-nutch") {
    description = "Stuff required by all targets"
    doLast {
        // making six directories
        mkdir(project.properties["build.dir"])
        mkdir(project.properties["build.classes"])
        mkdir(project.properties["release.dir"])
        mkdir(project.properties["test.build.dir"])
        mkdir(project.properties["test.build.classes"])
        mkdir(project.properties["test.build.lib.dir"])

        // renaming from *.template to * for all files in folders in conf.dir
        fileTree(project.properties["conf.dir"]).matching { include("**/*.template") }.forEach { file: File -> 
            rename { fileName: String ->
                fileName.replace(".template", "")
            }
        }
    }
}

tasks.register<Delete>("clean-default-lib")
{
    description = "Clean the project libraries directory (dependencies)"
    delete("${project.properties["build.lib.dir"]}")
}

tasks.register<Delete>("clean-test-lib")
{
    description = "Clean the project test libraries directory (dependencies)"
    delete("${project.properties["test.build.lib.dir"]}")
}

tasks.register<Delete>("clean-build")
{
    description = "Clean the project built files"
    delete("${project.properties["build.dir"]}")
}

tasks.register<Delete>("clean-dist")
{
    description = "Clean the project dist files"
    delete("${project.properties["dist.dir"]}")
}

tasks.register<Delete>("clean-runtime")
{
    description = "Clean the project runtime area"
    delete("${project.properties["runtime.dir"]}")
}

tasks.register<Copy>("copy-libs")
{
    description = "copy the libs in lib"
    from(layout.buildDirectory.dir("${project.properties["lib.dir"]}"))
    include("**/*.jar")
    into(layout.buildDirectory.dir("${project.properties["build.lib.dir"]}"))
}

tasks.register<GradleBuild>("compile-plugins")
{
    description = "compile plugins only"
    dependsOn("init","resolve-default")
    //TODO Once plugins are finished, uncomment the following lines:
    // dir = file("src/plugin")
    // tasks = listOf("deploy")
}

tasks.jar {
    description = "make nutch.jar"
    dependsOn("compile-core")

    from(layout.buildDirectory.dir("${project.properties["conf.dir"]}/nutch-default.xml"))
    into(layout.buildDirectory.dir("${project.properties["build.classes"]}"))

    from(layout.buildDirectory.dir("${project.properties["conf.dir"]}/nutch-site.xml"))
    into(layout.buildDirectory.dir("${project.properties["build.classes"]}"))

    //TODO this is meant to replace <jar jarfile="${build.dir}/${final.name}.jar" basedir="${build.classes}">
    destinationDirectory.set(file("${project.properties["build.dir"]}/${project.properties["final.name"]}.jar"))
    from(layout.buildDirectory.dir(project.properties["build.classes"] as String))
}

tasks.register<Copy>("runtime")
{
    description = "default target for running Nutch"
    dependsOn("jar","job")
    mkdir("${project.properties["runtime.dir"]}")
    mkdir("${project.properties["runtime.local"]}")
    mkdir("${project.properties["runtime.deploy"]}")

    from(layout.buildDirectory.dir("${project.properties["build.dir"]}/${project.properties["final.name"]}.job"))
    into(layout.buildDirectory.dir("${project.properties["runtime.deploy"]}"))

    from(layout.buildDirectory.dir("${project.properties["runtime.deploy"]}/bin"))
    into(layout.buildDirectory.dir("src/bin"))

    from(layout.buildDirectory.dir("${project.properties["build.dir"]}/${project.properties["final.name"]}.jar"))
    into(layout.buildDirectory.dir("${project.properties["runtime.local"]}/lib"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/lib/native"))
    into(layout.buildDirectory.dir("lib/native"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/conf")) {
        exclude("*.template")
    }
    into(layout.buildDirectory.dir("${project.properties["conf.dir"]}/lib"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/bin"))
    into(layout.buildDirectory.dir("src/bin"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/lib"))
    into(layout.buildDirectory.dir("${project.properties["build.dir"]}/lib"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/plugins"))
    into(layout.buildDirectory.dir("${project.properties["build.dir"]}/plugins"))

    from(layout.buildDirectory.dir("${project.properties["runtime.local"]}/test"))
    into(layout.buildDirectory.dir("${project.properties["build.dir"]}/test"))

    doLast() {
        project.exec() {
            commandLine("chmod","ugo+x","${project.properties["runtime.deploy"]}/bin")
            commandLine("chmod","ugo+x","${project.properties["runtime.local"]}/bin")
        }
    }
}