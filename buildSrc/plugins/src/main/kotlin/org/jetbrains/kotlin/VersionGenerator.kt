package org.jetbrains.kotlin

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File


open class VersionGenerator: DefaultTask() {
    @OutputDirectory
    val versionSourceDirectory = project.file("build/generated")
    @OutputFile
    val versionFile:File = project.file("${versionSourceDirectory.path}/org/jetbrains/kotlin/backend/konan/KonanVersion.kt")


    override fun configure(closure: Closure<*>?): Task {
        val result = super.configure(closure)
        doFirst {
            val content = buildString {
                operator fun String.unaryPlus() = this@buildString.append(this)
                val version = project.properties["konanVersion"].toString().split(".")
                val major = version[0].toInt()
                val minor = version[1].toInt()
                val maintenance = if (version.size > 2) version[2].toInt() else 0
                val build = System.getenv("BUILD_NUMBER")?.toInt() ?: -1
                val meta = project.properties["konanMetaVersion"]?.let { "MetaVersion.${it.toString().toUpperCase()}" }

                +""" package org.jetbrains.kotlin.backend.konan
             class KonanVersion(val meta: MetaVersion?, val major: Int, val minor: Int, val maintenance: Int, val build:Int) {
                 companion object {
                     val CURRENT = KonanVersion($meta, $major, $minor, $maintenance, $build)
                 }
                 override fun toString() = if (meta != null) "${'$'}meta ${'$'}major.${'$'}minor.${'$'}maintenance-${'$'}build" else "${'$'}major.${'$'}minor.${'$'}maintenance-${'$'}build"
             }
            """.trimMargin()
            }
            versionFile.printWriter().use {
                it.println(content)
            }
        }
        return result
    }
}
