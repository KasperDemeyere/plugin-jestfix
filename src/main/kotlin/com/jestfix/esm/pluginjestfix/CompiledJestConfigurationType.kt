package com.jestfix.esm.pluginjestfix

import com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationSingletonPolicy
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.NotNullLazyValue

class CompiledJestConfigurationType internal constructor() :
    SimpleConfigurationType(
        "JavaScriptTestRunnerCompiledJest",
        "CompiledJest",
        null,
        NotNullLazyValue.createValue { JfIcons.JestFix }), DumbAware {

    override fun getHelpTopic(): String {
        return "reference.dialogs.rundebug.JavaScriptTestRunnerJest"
    }

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return CompiledJestRunConfiguration(project, this, "CompiledJest")
    }

    override fun getSingletonPolicy(): RunConfigurationSingletonPolicy {
        return RunConfigurationSingletonPolicy.SINGLE_INSTANCE_ONLY
    }

    override fun isEditableInDumbMode(): Boolean {
        return true
    }

    companion object {
        private const val NAME: @NlsSafe String = "CompiledJest"

    }
}

fun getInstance(): CompiledJestConfigurationType {
    return findConfigurationType(CompiledJestConfigurationType::class.java)
}