package com.jestfix.esm.pluginjestfix

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.javascript.jest.JestRunConfiguration
import com.intellij.javascript.jest.JestRunSettings
import com.intellij.openapi.project.Project

class CompiledJestRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    JestRunConfiguration(project, factory, name) {

    override fun setRunSettings(settings: JestRunSettings) {
        val testFilePath = settings.testFileSystemDependentPath
        if (testFilePath.isNotEmpty()) {
            // Chose unit or int config file based on test file extension
            var configFilePath = settings.configFileSystemDependentPath
            configFilePath = if (testFilePath.endsWith("int.spec.ts")) {
                configFilePath.replace(Regex("/jest.*$"), "/jest.integration.build.config.js")
            } else {
                configFilePath.replace(Regex("/jest.*$"), "/jest.build.config.js")
            }

            // Updated test file path and extension so that it points to the compiled dist js test file
            val distFilePath = testFilePath.replace(Regex("/src/"), "/dist/")
            val jsDistFilePath = distFilePath.replace(Regex("ts$"), "js")

            val updatedSettings = settings.modify {
                it.setTestFilePath(jsDistFilePath)
                it.setConfigFilePath(configFilePath)
            }.builder().build()
            super.setRunSettings(updatedSettings)
        } else {
            super.setRunSettings(settings)
        }
    }
}
