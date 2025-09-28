package com.explore.lib_kcp

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class DebugLogComponentRegistrar : CompilerPluginRegistrar() {

    init {
        println(">>> DebugLogComponentRegistrar initialized")
    }

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration
    ) {
        println("DebugLogComponentRegistrar registerExtensions")
        IrGenerationExtension.registerExtension(DebugLogIrGenerationExtension())
    }
}