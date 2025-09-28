package com.explore.lib_plugin

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(KotlinCompilerPluginSupportPlugin::class)
class DebugLogGradleSubPlugin: KotlinCompilerPluginSupportPlugin {

    override fun apply(project: Project) {
        super.apply(project)
        val ext = project.extensions.create("debugLogConfig", DebugLogExtension::class.java)
        project.afterEvaluate {
            KcpLogger.bindProject(project)
            KcpLogger.debugEnabled = ext.enabled
            KcpLogger.info("DebugLogPlugin applied with debugEnabled=${ext.enabled}")
        }
        KcpLogger.info(">>> DebugLogGradleSubPlugin apply called")
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val extension = kotlinCompilation.project.extensions.findByType(DebugLogExtension::class.java)
        if (extension?.enabled == true) {
            println("plugin is enabled")
        }
        KcpLogger.info("DebugLogGradleSubPlugin applyToCompilation")
        val providerSubOption = kotlinCompilation.project.provider {
            buildList {
                // enabled 参数（可选）
                add(SubpluginOption("enabled", extension?.enabled.toString()))
                extension?.debugLogAnnotation?.forEach {
                    add(SubpluginOption("debugLogAnnotation", it))
                }
            }
        }
        KcpLogger.info("DebugLogGradleSubPlugin applyToCompilation subOptions = ${providerSubOption.get().size}")
        return providerSubOption
    }

    override fun getCompilerPluginId(): String {
        return "debuglog"
    }

    override fun getPluginArtifact(): SubpluginArtifact {
        return SubpluginArtifact(
            groupId = "com.explore.debuglog.kcp",
            artifactId = "plugin-kcp",
            version = "0.0.1"
        )
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        KcpLogger.info("DebugLogGradleSubPlugin isApplicable = " + kotlinCompilation.project.plugins.hasPlugin(DebugLogGradleSubPlugin::class.java))
        return true
    }
}