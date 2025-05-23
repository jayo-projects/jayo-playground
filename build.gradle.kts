import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.DontIncludeResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.IncludeResourceTransformer
import org.gradle.api.file.DuplicatesStrategy.WARN
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import kotlin.jvm.optionals.getOrNull

val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun catalogVersion(lib: String) =
    versionCatalog.findVersion(lib).getOrNull()?.requiredVersion
        ?: throw GradleException("Version '$lib' is not specified in the toml version catalog")

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jspecify)

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.slf4j)
    testRuntimeOnly(libs.logback)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

jmh {
    jvmArgs = listOf("-Djmh.separateClasspathJAR=true", "-Dorg.gradle.daemon=false", "-Djmh.executor=VIRTUAL")
    duplicateClassesStrategy = WARN
    jmhVersion = catalogVersion("jmh")

    includes.set(listOf("""jayo\.playground\.benchmarks\.BufferReaderUtf8Benchmark.*"""))
//    includes.set(listOf("""jayo\.playground\.benchmarks\.SlowReaderBenchmark.*"""))
//    includes.set(listOf("""jayo\.playground\.benchmarks\.TaskRunnerBenchmark.*"""))
}

tasks {
    val shadowJmh by registering(ShadowJar::class) {
        dependsOn("jmhJar")

        transform(DontIncludeResourceTransformer().apply {
            resource = "META-INF/BenchmarkList"
        })

        transform(IncludeResourceTransformer().apply {
            resource = "META-INF/BenchmarkList"
            file = file("${project.layout.buildDirectory.get()}/jmh-generated-resources/META-INF/BenchmarkList")
        })
    }

    val assemble by getting {
        dependsOn(shadowJmh)
    }
}

// when version changes :
// -> execute ./gradlew wrapper, then remove .gradle directory, then execute ./gradlew wrapper again
tasks.wrapper {
    gradleVersion = "8.12.1"
    distributionType = Wrapper.DistributionType.ALL
}
