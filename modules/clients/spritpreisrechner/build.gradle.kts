/*
 * Copyright 2022 mbodev @ https://mbo.dev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("java-library")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
}

dependencies {
    api(project(":client-common"))
    testImplementation(project(":common-test"))
}

val openAPIClientPackage: String by project
val openAPIBasePackage = "${openAPIClientPackage}.spritpreisrechner"
val openAPISpecFilePath: String by project
val openAPIGenOutBase: String by project
val openAPISpecFileNamePrefix: String by project
val openAPISpecFileName = "${openAPISpecFileNamePrefix}${project.name}"
val openAPISpecFileExt = "json"

val openApiSrcDir = "${project.buildDir}/$openAPIGenOutBase/src/main/kotlin"
sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin", openApiSrcDir)
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
}

openApiGenerate {
    // https://openapi-generator.tech/docs/generators/kotlin-spring
    generatorName.set("kotlin-spring")
    library.set("spring-boot")
    templateDir.set("${project.rootDir}/modules/clients/client-common/$openAPISpecFilePath/templates")

    inputSpec.set("$projectDir/$openAPISpecFilePath/$openAPISpecFileName.$openAPISpecFileExt")
    outputDir.set("$buildDir/$openAPIGenOutBase")

    packageName.set(openAPIBasePackage)
    apiPackage.set("$openAPIBasePackage.api")
    invokerPackage.set("$openAPIBasePackage.client")
    modelPackage.set("$openAPIBasePackage.model")

    modelNameSuffix.set("Dto")
    typeMappings.set(
        mapOf(
            "DateTime" to "Instant"
        )
    )
    importMappings.set(
        mapOf(
            "Instant" to "java.time.Instant"
        )
    )
    configOptions.set(
        mapOf(
            "useBeanValidation" to "true",
            "delegatePattern" to "true",
            "useTags" to "true",
            "exceptionHandler" to "false",
            "gradleBuildFile" to "false",
            "sortModelPropertiesByRequiredFlag" to "true",
            "sortParamsByRequiredFlag" to "true",
        )
    )
}

val openApiClean = tasks.create<Delete>("openApiClean") {
    group = "openapi tools"
    delete("$openApiSrcDir/${openAPIBasePackage.replace(".", "/")}")
}

val openApiGenerateTask = tasks.getByName("openApiGenerate")
val compileKotlinTask = tasks.getByName("compileKotlin")

openApiGenerateTask.dependsOn(openApiClean)
compileKotlinTask.dependsOn(openApiGenerateTask)
