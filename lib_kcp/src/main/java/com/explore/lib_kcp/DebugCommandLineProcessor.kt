package com.explore.lib_kcp

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey


@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class DebugCommandLineProcessor : CommandLineProcessor {
    init {
        println(">>> DebugCommandLineProcessor initialized")
    }

    override val pluginId: String = "debuglog"
    override val pluginOptions: Collection<AbstractCliOption> = listOf<AbstractCliOption>(
        CliOption(
            "enabled",
            "<true|false>",
            "是否开启debuglog",
            required = false,
            allowMultipleOccurrences = false
        ),
        CliOption(
            "debugLogAnnotation", "<fqname>", "debug-log annotation names",
            required = false, allowMultipleOccurrences = true
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        println(">>> DebugCommandLineProcessor processOption")
        when(option.optionName) {
            "enabled"-> {
                configuration.put(CompilerConfigurationKey(Constants.KEY_ENABLED), value.toBoolean())
            }
            "debugLogAnnotation" -> configuration.appendList(CompilerConfigurationKey(Constants.KEY_ANNOTATIONS), value)
            else -> error("Unexpected config option ${option.optionName}")
        }
        super.processOption(option, value, configuration)
    }
}