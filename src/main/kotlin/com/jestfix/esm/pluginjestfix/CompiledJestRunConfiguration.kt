package com.jestfix.esm.pluginjestfix

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.javascript.jest.JestRunConfiguration
import com.intellij.openapi.project.Project

class CompiledJestRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    JestRunConfiguration(project, factory, name) {
}
