package com.explore.lib_plugin

import org.gradle.api.Project

/**
 * 统一日志工具，支持开关
 */
object KcpLogger {

    private var gradleLogger: org.gradle.api.logging.Logger? = null

    // 日志开关，默认关闭
    var debugEnabled: Boolean = false

    /**
     * 绑定 Gradle 的 logger
     */
    fun bindProject(project: Project) {
        gradleLogger = project.logger
    }

    fun info(msg: String) {
        if (debugEnabled) {
            gradleLogger?.lifecycle("[KCP] $msg")
        }
    }

    fun warn(msg: String) {
        if (debugEnabled) {
            gradleLogger?.warn("[KCP] $msg")
        }
    }

    fun error(msg: String) {
        gradleLogger?.error("[KCP] $msg")
    }
}