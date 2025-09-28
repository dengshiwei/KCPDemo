package com.explore.lib_kcp

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.FirIncompatiblePluginAPI
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * 插件入口，注册 IR 扩展
 */
class DebugLogIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        println("DebugLogIrGenerationExtension generate")
        moduleFragment.transformChildrenVoid(FunctionNamePrinter(pluginContext))
    }
}

/**
 * IR 转换器：遍历函数，打印函数名称
 */
class FunctionNamePrinter(private val pluginContext: IrPluginContext) :
    IrElementTransformerVoidWithContext() {

    private val functionStack = ArrayDeque<String>()

    @OptIn(FirIncompatiblePluginAPI::class)
    private val printlnSymbol: IrSimpleFunctionSymbol by lazy {
        val printlnCallableId = CallableId(
            packageName = FqName("kotlin.io"), // println 所在的包
            callableName = Name.identifier("println")
        )
        val candidates = pluginContext.referenceFunctions(printlnCallableId)

        val match = candidates.firstOrNull { symbol ->
            val fn = symbol.owner
            fn.valueParameters.size == 1 &&
                    fn.valueParameters[0].type.classFqName == FqName("kotlin.Any")
        } ?: error("没找到 println(Any?)，候选有：${candidates.map { it.owner.render() }}")

        match
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        val functionName = declaration.name.asString()
        functionStack.addLast(functionName)

        // 编译期日志
        System.err.println("[KCP] visiting function: $functionName")

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        // 在函数开头插入 println
        declaration.body?.let { body ->
            val newBody = builder.irBlockBody(declaration.startOffset, declaration.endOffset) {
                +irCall(printlnSymbol).apply {
                    putValueArgument(0, irString(">>> Enter $functionName"))
                }
                body.statements.forEach { +it }
            }
            declaration.body = newBody
        }

        val result = super.visitFunctionNew(declaration)
        functionStack.removeLast()
        return result
    }
}
