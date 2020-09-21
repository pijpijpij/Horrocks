import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright 2018, Chiswick Forest
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */


plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("maven")
}


task("preCommit") {
    group = "verification"
    description = "Runs the tasks that must be passed before committing changes."
}.dependsOn("test")

val kotlin_version = rootProject.extra["kotlin_version"]
val rxKotlinVersion = rootProject.ext["rxkotlinVersion"]
val rxjava2Version = rootProject.ext["rxjava2Version"]
val junitVersion = "5.6.2"
val mockitoVersion = rootProject.ext["mockitoVersion"]
val mockitoKotlinVersion = rootProject.ext["mockitoKotlinVersion"]
val slf4jVersion = "1.7.9"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")

    // Basic utilities
    api("io.reactivex.rxjava2:rxjava:$rxjava2Version")
    implementation("io.reactivex.rxjava2:rxkotlin:$rxKotlinVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.slf4j:slf4j-simple:$slf4jVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

