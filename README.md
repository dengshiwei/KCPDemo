### Kotlin Compiler
在 Android 和 Kotlin 项目中，有时候我们需要在编译阶段做一些事情，例如：

- 在字节码或 IR (Intermediate Representation) 阶段插入逻辑
- 自动生成样板代码
- 做埋点、日志打印等增强功能
- 
Kotlin 官方提供了 Kotlin Compiler Plugin (KCP) 机制，使我们可以在编译时插手 Kotlin 编译流程，实现定制化逻辑。

KCP 的介绍可参照这篇博客：https://juejin.cn/post/7153076275207208991?searchId=20250928171313AB44AF15E9EE1B8028DA

### 架构
<img width="1002" height="886" alt="image" src="https://github.com/user-attachments/assets/f0b290b6-6b77-4601-8da5-91975c532a7a" />

在上面的架构中，


Gradle 插件 vs Kotlin Compiler 插件
<img width="1624" height="550" alt="image" src="https://github.com/user-attachments/assets/1cd0c184-7297-4c2d-a3e6-a0529ef39ef9" />



#### 三者之间的关系
sample-app (使用插件)
       │
       ▼
kcp-gradle-plugin  (Gradle 插件, 提供 id=... )
       │
       │ 调用 getPluginArtifact() → 指定 groupId/artifactId/version
       ▼
kcp-plugin (Kotlin 编译器插件本体, 被 Gradle 插件加载)

**总结**

getPluginArtifact() = kcp-plugin 发布出来的 Maven 坐标

kcp-plugin 必须用 maven-publish 配置 groupId/artifactId/version

kcp-gradle-plugin 不关心 kcp-plugin 的源码，只需要把它的坐标告诉 Kotlin 编译器

sample-app 使用的是 kcp-gradle-plugin 的 id("...")

## 实现步骤

### 1. 项目结构

一个完整的 KCP 项目一般包含三个模块：

```
KCPDemo/
├─ lib_kcp/ # KCP 核心模块：CommandLineProcessor + ComponentRegistrar + IR Transform
├─ lib_plugin/ # Gradle 插件模块：桥接 lib_kcp 和 Gradle
└─ app/ # 测试应用模块
```


- `lib_kcp`：负责实际的编译期逻辑。
- `lib_plugin`：提供 Gradle 插件，让应用项目直接使用。
- `app`：Demo 应用，用于验证插件效果。

---

### 2. 搭建 lib_kcp

#### build.gradle.kts

```kotlin
plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.explore.debuglog.kcp"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "plugin-kcp"
            version = version.toString()
        }
    }
}
```

### 3. 核心组件

#### 3.1 CommandLineProcessor

处理插件传入参数：

```
class DebugCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "com.explore.debuglog.kcp"
    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption("enabled", "<true|false>", "Enable debug log plugin")
    )
}
```

#### 3.2 ComponentRegistrar

注册编译期扩展，例如 IR Transformer：

```
class DebugLogComponentRegistrar(
    private val enabled: Boolean
) : CompilerPluginRegistrar() {

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (enabled) {
            IrGenerationExtension.registerExtension(DebugLogIrGenerationExtension())
        }
    }
}
```

#### 3.3 IrGenerationExtension
执行 IR 插码逻辑：

```
class DebugLogIrGenerationExtension : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transform(DebugLogTransformer(pluginContext), null)
    }
}
```

### 4. 插码示例：打印函数名

```
class DebugLogTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {
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
        val body = declaration.body as? IrBlockBody ?: return super.visitFunctionNew(declaration)

        val builder = DeclarationIrBuilder(pluginContext, declaration.symbol)

        val logStatement = builder.irCall(printlnSymbol).apply {
            putValueArgument(0, builder.irString(">>> Entering function: ${declaration.name}"))
        }

        body.statements.add(0, logStatement)
        return super.visitFunctionNew(declaration)
    }
}
```

这样，每个函数在运行时会打印自己的名称。

### 5. lib_plugin：Gradle 插件桥接

```
class DebugLogGradleSubPlugin : KotlinCompilerPluginSupportPlugin {
    override fun getCompilerPluginId(): String = "com.explore.debuglog.kcp"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact("com.explore.debuglog.kcp", "plugin-kcp", "0.0.1")

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        kotlinCompilation.target.project.provider {
            listOf(SubpluginOption("enabled", "true"))
        }
}
```

在 plugins {} 中使用：

```
plugins {
    id("com.explore.plugin") version "0.0.1"
}
```

### 6. 调试方法

在 Android Studio 默认运行中，插件日志可能不显示。
推荐使用命令行 in-process 编译查看日志：

```
./gradlew :app:compileDebugKotlin \
  --stacktrace \
  --info \
  -Pkotlin.compiler.execution.strategy=in-process
```

### 7. 常见问题

CommandLineProcessor 或 ComponentRegistrar 不执行

- 确认 META-INF/services 正确：

```
org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
referenceFunctions 找不到函数
```

- K2 下需要用 CallableId + 手动筛选参数：

```
symbol.owner.valueParameters.size == 1 &&
symbol.owner.valueParameters[0].type.classFqName == FqName("kotlin.Any")
```

### 8. 总结

KCP 开发流程：

- 搭建 lib_kcp + lib_plugin 工程
- 实现 CommandLineProcessor、ComponentRegistrar、IrGenerationExtension
- 编写 IR Transformer 插码
- Gradle 插件桥接 KCP，传递参数
- in-process 编译调试插件

通过以上步骤，就可以实现 编译时自动插码、日志打印或埋点 功能。



