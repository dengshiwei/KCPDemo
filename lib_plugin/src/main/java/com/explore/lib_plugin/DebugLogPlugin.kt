package com.explore.lib_plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class DebugLogPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        KcpLogger.info("DebugLogPlugin apply")
        project.extensions.create("debugLog", DebugLogExtension::class.java)
    }
}