import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

logger.quiet("Java version: ${JavaVersion.current()}")
logger.quiet("Gradle version: ${gradle.gradleVersion}")

plugins {
  id("java-library")
  id("com.diffplug.gradle.spotless") version "6.22.0" apply (false)
  id("com.github.spotbugs") version "5.1.5" apply (false)
  id("com.asarkar.gradle.build-time-tracker") version "4.3.0"
}

allprojects {
  group = "edu.paschalcs"
  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "java")
  configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  apply(plugin = "com.diffplug.spotless")
  configure<SpotlessExtension> {
    java {
      removeUnusedImports()
      googleJavaFormat()
    }
  }

  apply(plugin = "checkstyle")
  configure<CheckstyleExtension> {
    toolVersion = "10.12.0"
    configFile = rootProject.file("./checkstyle.xml")
    maxErrors = 0
    maxWarnings = 0
    isIgnoreFailures = false
  }

  apply(plugin = "com.github.spotbugs")
  configure<SpotBugsExtension> {
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    ignoreFailures.set(false)
    excludeFilter.set(rootProject.file("./spotbugs-exclude.xml"))
  }
  tasks.withType<SpotBugsTask> {
    reports.create("html").required.set(true)
  }

  tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
    testLogging {
      events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
      afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) {
          println(
            "Results: ${result.resultType} " +
                "(${result.testCount} test${if (result.testCount > 1) "s" else ""}, " +
                "${result.successfulTestCount} passed, " +
                "${result.failedTestCount} failed, " +
                "${result.skippedTestCount} skipped)"
          )
        }
      }))
    }
    finalizedBy(tasks.withType<JacocoReport>())
  }

  apply(plugin = "jacoco")
  tasks.withType<JacocoReport> {
    reports {
      xml.required.set(true)
    }
  }

  val previewFeatures = emptyList<String>()
  tasks.withType<JavaCompile> {
    options.compilerArgs = previewFeatures
  }
  tasks.withType<Test> {
    jvmArgs = previewFeatures
  }
  tasks.withType<JavaExec> {
    jvmArgs = previewFeatures
  }

  dependencies {
    
    val guavaVersion = "32.1.3-jre"
    val logbackVersion = "1.5.4"
    val corenlpVersion = "4.5.6"
    val commons_csvVersion = "1.10.0"
    val jacksonVersion = "2.17.0"
    val commons_textVersion = "1.11.0"
    val n4jVersion = "5.18.1"
    val commons_cliVersion = "1.6.0"
    val retrofitVersion = "2.11.0"
    val commons_emailVersion = "1.6.0"
    val webDriver_version = "4.19.1"

    implementation("com.github.spotbugs:spotbugs-annotations:4.8.0")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("edu.stanford.nlp:stanford-corenlp:$corenlpVersion")
    implementation("org.apache.commons:commons-csv:$commons_csvVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.apache.commons:commons-text:$commons_textVersion")
    implementation("org.neo4j:neo4j-graph-algo:$n4jVersion")
    implementation("commons-cli:commons-cli:$commons_cliVersion")
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("org.apache.commons:commons-email:$commons_emailVersion")
    implementation("org.seleniumhq.selenium:selenium-java:$webDriver_version")
    implementation("org.seleniumhq.selenium:selenium-api:$webDriver_version")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:$webDriver_version")


    val junitVersion = "5.10.0"
    val truthVersion = "1.1.5"
    val mockitoVersion = "5.6.0"

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("com.google.truth:truth:$truthVersion")
    testImplementation("com.google.truth.extensions:truth-java8-extension:$truthVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")

    configurations.all {
      exclude("org.assertj")
      exclude("junit")
      resolutionStrategy {
        force("com.google.guava:guava:$guavaVersion") // exclude android version
      }
    }
  }
}
