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
// ant.importBuild("build.xml") {old ->"ant-${old}"}

// the normal classpath
val classpathCollection: FileCollection = layout.files(
    file("${project.properties["build.classes"]}"),
    fileTree(mapOf("dir" to project.properties["build.lib.dir"], "include" to listOf("*.jar")))
)
val classPath: String = classpathCollection.asPath

// test classpath
val testClasspathCollection: FileCollection = layout.files(
    file("${project.properties["test.build.classes"]}"),
    file("${project.properties["conf.dir"]}"),
    file("${project.properties["test.src.dir"]}"),
    file("${project.properties["build.plugins"]}"),
    classpathCollection,
    file(layout.buildDirectory.dir("${project.properties["build.dir"]}/${project.properties["final.name"]}.job")),
    fileTree(mapOf("dir" to project.properties["build.lib.dir"], "include" to listOf("*.jar"))),
    fileTree(mapOf("dir" to project.properties["test.build.lib.dir"], "include" to listOf("*.jar")))
)

// ant target "init" renamed to "init-nutch" to avoid gradle naming conflits
tasks.register<Copy>("init-nutch") {
    description = "Stuff required by all targets"

    // making six directories
    mkdir("${project.properties["build.dir"]}")
    mkdir("${project.properties["build.classes"]}")
    mkdir("${project.properties["build.dir"]}/release")
    mkdir("${project.properties["test.build.dir"]}")
    mkdir("${project.properties["test.build.classes"]}")
    mkdir("${project.properties["test.build.lib.dir"]}")

    // renaming from *.template to * for all files in folders in conf.dir
    fileTree("${project.properties["conf.dir"]}").matching { include("**/*.template") }.forEach { file: File ->
        rename { fileName: String ->
            fileName.replace(".template", "")
        }
    }
}

tasks.register("resolve-default")
{
    description = "Resolve and retrieve dependencies"
    dependsOn("clean-default-lib","init-nutch","copy-libs")
}

tasks.register("compile")
{
    description = "Compile all Java files"
    dependsOn("compile-core","compile-plugins")
}

tasks.register<JavaCompile>("compile-core") 
{
    description = "Compile core Java files only"
    dependsOn("init-nutch","resolve-default")

    source = fileTree(layout.buildDirectory.dir("${project.properties["src.dir"]}"))
    include("org/apache/nutch/**/*.java")
    destinationDirectory.set(layout.buildDirectory.dir("${project.properties["build.classes"]}"))
    classpath = classpathCollection
    sourceCompatibility = "${project.properties["javac.version"]}"
    targetCompatibility = "${project.properties["javac.version"]}"
    
    options.annotationProcessorPath = classpathCollection
    options.sourcepath = layout.files("${project.properties["src.dir"]}")
    options.compilerArgs.add("-Xlint-path")
    options.encoding = "${project.properties["build.encoding"]}"
    options.isDebug = "${project.properties["javac.debug"]}" == "on"
    options.isDeprecation = "${project.properties["javac.deprecation"]}" == "on"

    copy {
        from(layout.buildDirectory.dir("${project.properties["src.dir"]}"))
        include("**/*.html")
        include("**/*.css")
        include("**/*.properties")
        into(layout.buildDirectory.dir("${project.properties["build.classes"]}"))
    }
}

tasks.register<JavaExec>("proxy") 
{
    description = "Run nutch proxy"
    dependsOn("compile-core-test","job")

    mainClass.set("org.apache.nutch.tools.proxy.ProxyTestbed")
    classpath = testClasspathCollection
    args("-fake")
    jvmArgs("-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")
}

tasks.register<JavaExec>("benchmark")
{
    description = "Run nutch benchmarking analysis"

    mainClass.set("org.apache.nutch.tools.Benchmark")
    classpath = testClasspathCollection
    jvmArgs("-Xmx512m -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl")
    args("-maxPerHost")
    args("10")
    args("-seeds")
    args("1")
    args("-depth")
    args("5")
}

/*
tasks.javadoc {
    description = "Generate Javadoc"
    mkdir("${project.properties["build.javadoc"]}")
    mkdir("${project.properties["build.javadoc"]}/resources")
}
*/

tasks.clean {
    description = "Clean the project"
    dependsOn("clean-build","clean-lib","clean-dist","clean-runtime")
}

tasks.register("clean-lib") {
    description = "Clean the project libraries directories (dependencies: default + test)"
    dependsOn("clean-default-lib","clean-test-lib")
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
    description = "Copy the libs in lib"
    from(layout.buildDirectory.dir("${project.properties["lib.dir"]}"))
    include("**/*.jar")
    into(layout.buildDirectory.dir("${project.properties["build.lib.dir"]}"))
}

tasks.register<GradleBuild>("compile-plugins")
{
    description = "Compile plugins only"
    dependsOn("init-nutch","resolve-default")
    //TODO Once plugins are finished, uncomment the following lines:
    // dir = file("src/plugin")
    // tasks = listOf("deploy")
}

tasks.jar {
    description = "Make nutch.jar"
    dependsOn("compile-core")

    from("${project.properties["conf.dir"]}/nutch-default.xml")
    into("${project.properties["build.classes"]}")

    from("${project.properties["conf.dir"]}/nutch-site.xml")
    into("${project.properties["build.classes"]}")

    //TODO this is meant to replace <jar jarfile="${build.dir}/${final.name}.jar" basedir="${build.classes}">
    destinationDirectory.set(file("${project.properties["build.dir"]}/${project.properties["final.name"]}.jar"))
    from(project.properties["build.classes"] as String)
}

tasks.register<Copy>("runtime")
{
    description = "Default target for running Nutch"
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

tasks.register<Jar>("job")
{
    //TODO there is no support to create a ".job" directly
    description = "Make nutch.job jar"
    dependsOn("compile")

    from(
        files("${project.properties["build.classes"]}") {
            exclude("nutch-default.xml","nutch-site.xml")
        },
        files("${project.properties["conf.dir"]}") {
            exclude("*.template","hadoop*.*")
        },
        files("${project.properties["build.lib.dir"]}") {
            eachFile {
                relativePath = RelativePath(true,"lib")
            }
            include("**/*.jar")
            exclude("hadoop-*.jar,slf4j*.jar","log4j*.jar")
        },
        files("${project.properties["build.plugins"]}") {
            eachFile {
                relativePath = RelativePath(true,"classes","plugins")
            }
        }
    )
    into("${project.properties["build.dir"]}/${project.properties["final.name"]}.job")
}

tasks.register<JavaCompile>("compile-core-test")
{
    description = "Compile test code"
    dependsOn("init-nutch","compile-core","resolve-test")
    source = fileTree(layout.buildDirectory.dir("${project.properties["test.src.dir"]}"))
    include("org/apache/nutch/**/*.java")
    destinationDirectory.set(layout.projectDirectory.dir("${project.properties["build.classes"]}"))
    classpath = classpathCollection
    sourceCompatibility = "${project.properties["javac.version"]}"
    targetCompatibility = "${project.properties["javac.version"]}"

    options.annotationProcessorPath = classpathCollection
    options.sourcepath = layout.files("${project.properties["src.dir"]}")
    options.compilerArgs.add("-Xlint:-path")
    options.isDebug = "${project.properties["javac.debug"]}" == "on"
    options.encoding = "${project.properties["build.encoding"]}"
    options.isDeprecation = "${project.properties["javac.deprecation"]}" == "on"
}

tasks.test.configure()
{
    description = "Run JUnit tests"
    dependsOn("test-core","test-plugins")
}