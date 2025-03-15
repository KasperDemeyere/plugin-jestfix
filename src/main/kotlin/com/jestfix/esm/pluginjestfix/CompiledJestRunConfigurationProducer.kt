package com.jestfix.esm.pluginjestfix

import com.amazon.ion.shaded_.do_not_use.kotlin.jvm.internal.Intrinsics
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.javascript.jest.JestRunConfiguration
import com.intellij.javascript.jest.JestRunConfigurationProducer
import com.intellij.javascript.jest.JestRunSettings
import com.intellij.javascript.jest.JestUtil
import com.intellij.javascript.testFramework.AbstractTestFileStructure
import com.intellij.javascript.testFramework.JsTestElementPath
import com.intellij.javascript.testFramework.JsTestFileByTestNameIndex
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructure
import com.intellij.javascript.testFramework.jasmine.JasmineFileStructureBuilder
import com.intellij.javascript.testing.runScope.JsTestRunScope
import com.intellij.javascript.testing.runScope.JsTestRunScopeKind
import com.intellij.json.psi.JsonProperty
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSTestFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable


class CompiledJestRunConfigurationProducer: JestRunConfigurationProducer() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        val configurationType: CompiledJestConfigurationType = getInstance()
        Intrinsics.checkNotNullExpressionValue(configurationType, "getInstance(...)")
        return configurationType
    }

    /**
     * Set up a run configuration from a certain context (e.g. right-clicking a test class/method, gutter run click,...)
     */
    override fun setupConfigurationFromCompatibleContext(
        configuration: JestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        Intrinsics.checkNotNullParameter(configuration, "configuration")
        Intrinsics.checkNotNullParameter(context, "context")
        Intrinsics.checkNotNullParameter(sourceElement, "sourceElement")
        val element = context.psiLocation
        if (element != null && this.isTestRunnerAvailableFor(element, context)) {
            val jestRunSettings = configuration.runSettings
            Intrinsics.checkNotNullExpressionValue(jestRunSettings, "getRunSettings(...)")
            val extendedSettings = this.buildContextSettings(element, jestRunSettings)
            if (extendedSettings == null) {
                return false
            } else {
                println("Inside: setupConfigurationFromCompatibleContext")
                // Modify and set run settings with compiled path and config corresponding to test kind (unit or int)
                val runSettings = extendedSettings.getSettings()
                configuration.runSettings = runSettings.modify {
                    it.setConfigFilePath(TestPathUtil.getConfigFile(runSettings.workingDirSystemDependentPath, it.scope.testFilePath))
                    it.setTestFilePath(TestPathUtil.replaceWithCompiledPath(it.scope.testFilePath))
                }
                sourceElement.set(extendedSettings.getEnclosingElement())
                configuration.setGeneratedName()
                return true
            }
        } else {
            return false
        }
    }

    /**
     * Check if a test configuration already exists for the given context.
     * This will prevent the creation of duplicate test run configurations and will make it so the existing run configuration is automatically reused.
     */
    override fun isConfigurationFromCompatibleContext(
        configuration: JestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        Intrinsics.checkNotNullParameter(configuration, "configuration")
        Intrinsics.checkNotNullParameter(context, "context")
        val element = context.psiLocation
        if (element == null) {
            return false
        } else {
            val runConfiguration: RunConfiguration = cloneTemplateConfiguration(context).configuration
            if (runConfiguration !is CompiledJestRunConfiguration) {
                return false
            } else {
                val jestRunSettings: JestRunSettings = runConfiguration.runSettings
                Intrinsics.checkNotNullExpressionValue(jestRunSettings, "getRunSettings(...)")
                val extendedSettings: ExtendedSettings? = this.buildContextSettings(element, jestRunSettings)
                if (extendedSettings == null) {
                    return false
                } else {
                    val generatedSettings = extendedSettings.getSettings()
                    val existingSettings: JestRunSettings = configuration.runSettings
                    Intrinsics.checkNotNullExpressionValue(existingSettings, "getRunSettings(...)")
                    return this.areConfigFilesEffectivelySame(generatedSettings, existingSettings) &&
                            generatedSettings.workingDirSystemDependentPath == existingSettings.workingDirSystemDependentPath &&
                            this.areTestSetupsEffectivelySame(generatedSettings.scope, existingSettings.scope)
                }
            }
        }
    }

    /**
     * Check that the newly generated config settings and an existing config setting are as good as equal.
     * This returns true if the generated settings have a configFile equal to the existing one, or the generated one is empty and the existing one is not
     */
    private fun areConfigFilesEffectivelySame(
        generatedSettings: JestRunSettings,
        existingSettings: JestRunSettings
    ): Boolean {
        return generatedSettings.configFileSystemDependentPath == existingSettings.configFileSystemDependentPath ||
                (generatedSettings.configFileSystemDependentPath.isEmpty() && (existingSettings.configFileSystemDependentPath).isNotEmpty())
    }

    /**
     * The generated settings have a scope similar to the existing one.
     * Similar meaning that:
     * - their kinds are equal
     * - their testNames are equal
     * - their testDirectoryPaths are equal
     * - their testFilePaths are similar: src & dist are seen as equal and the js and ts extension in the path too.
     */
    private fun areTestSetupsEffectivelySame(
        generatedSettingScope: JsTestRunScope,
        existingSettingScope: JsTestRunScope
    ): Boolean {
        val similarTestSetup = generatedSettingScope.kind == existingSettingScope.kind &&
                generatedSettingScope.testNames == existingSettingScope.testNames &&
                generatedSettingScope.testDirectoryPath == existingSettingScope.testDirectoryPath
        val generatedCompiledJsTestFilePath = TestPathUtil.replaceWithCompiledPath(generatedSettingScope.testFilePath)
        val existingCompiledJsTestFilePath = TestPathUtil.replaceWithCompiledPath(existingSettingScope.testFilePath)
        return similarTestSetup && generatedCompiledJsTestFilePath == existingCompiledJsTestFilePath
    }

    private fun buildContextSettings(element: PsiElement, templateRunSettings: JestRunSettings): ExtendedSettings?  {
        val psiFile: PsiFile = element.containingFile
        val virtualFile: VirtualFile? = PsiUtilCore.getVirtualFile(element)
        if (virtualFile == null) {
            return null
        } else {
            val context = Context(element, psiFile, virtualFile, templateRunSettings)
            if (element is PsiDirectory) {
                return this.buildDirectorySettings(element, context)
            } else {
                var extendedSettings: ExtendedSettings? = this.buildSuiteOrTestSettings(context)
                return if (extendedSettings != null) {
                    extendedSettings
                } else {
                    extendedSettings = this.buildConfigSettings(context)
                    extendedSettings ?: this.buildTestFileSettings(context)
                }
            }
        }
    }

    private fun buildTestFileSettings(context: Context): ExtendedSettings? {
        val psiFile = context.psiFile
        val testFileType = if (psiFile is JSFile) psiFile.testFileType else null
        if (testFileType == JSTestFileType.JASMINE) {
            val builder: JestRunSettings.Builder = context.templateRunSettings.builder()
                .scope { it: JsTestRunScope.Builder ->
                    Intrinsics.checkNotNullParameter(it, "it")
                    it.kind(JsTestRunScopeKind.TEST_FILE)
                    val contextFilePath = context.file.path
                    Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
                    it.testFilePath(contextFilePath)
                }
            return ExtendedSettings(
                this,
                builder.build(),
                context.file,
                context.psiFile
            )
        } else {
            return null
        }
    }

    private fun buildConfigSettings(context: Context): ExtendedSettings? {
        println("Inside buildConfigSettings")
        if (JestUtil.isJestConfigFile(context.file.name as CharSequence)) {
            val settings = context.templateRunSettings.builder()
                .scope { it: JsTestRunScope.Builder ->
                    Intrinsics.checkNotNullParameter(it, "it")
                    it.kind = JsTestRunScopeKind.ALL
                }
                .build()
            return ExtendedSettings(
                this,
                settings,
                context.file,
                context.psiFile
            )
        } else {
            if (PackageJsonUtil.isPackageJsonFile(context.psiFile)) {
                val jestProp: JsonProperty? = PackageJsonUtil.findContainingTopLevelProperty(context.element)
                if (jestProp != null && "jest" == jestProp.name) {
                    val settings = context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
                        Intrinsics.checkNotNullParameter(it, "it")
                        it.kind = JsTestRunScopeKind.ALL
                    }.build()
                    return ExtendedSettings(
                        this,
                        settings,
                        context.file,
                        jestProp as PsiElement
                    )
                }
            }

            return null
        }
    }

    private fun buildSuiteOrTestSettings(context: Context): ExtendedSettings? {
        val testPath = this.findContextSuiteOrTestPath(context.element)
        if (testPath == null) {
            return null
        } else {
            val jestRunSettingsBuilder: JestRunSettings.Builder = context.templateRunSettings.builder()
            var jsTestRunScopeBuilder: JsTestRunScope.Builder = jestRunSettingsBuilder.scope.builder()
            val contextFilePath = context.file.path
            Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
            jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testFilePath(contextFilePath).build())
            val testName = testPath.testName
            val testNames: MutableList<String> = testPath.suiteNames.toMutableList()
            Intrinsics.checkNotNullExpressionValue(testNames, "getSuiteNames(...)")
            if (testName == null) {
                jsTestRunScopeBuilder = jestRunSettingsBuilder.scope.builder().kind(JsTestRunScopeKind.SUITE)
                jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testNames(testNames).build())
            } else {
                jsTestRunScopeBuilder = jestRunSettingsBuilder.scope.builder().kind(JsTestRunScopeKind.TEST)
                testNames.add(testName)
                jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testNames(testNames).build())
            }

            val jestRunSettings: JestRunSettings = jestRunSettingsBuilder.build()
            val testElementPath = testPath.testElement
            Intrinsics.checkNotNullExpressionValue(testElementPath, "getTestElement(...)")
            return ExtendedSettings(this, jestRunSettings, context.file, testElementPath)
        }
    }

    private fun findContextSuiteOrTestPath(element: PsiElement): JsTestElementPath? {
        if (element is PsiFileSystemItem) {
            return null
        } else {
            val containingJsFile = this.getContainingJsFile(element)
            if (containingJsFile == null) {
                return null
            } else {
                val jsFile: JSFile = containingJsFile
                val textRange: TextRange = element.textRange
                val testFileStructure: AbstractTestFileStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
                Intrinsics.checkNotNullExpressionValue(testFileStructure, "fetchCachedTestFileStructure(...)")
                val jasmineStructure = testFileStructure as JasmineFileStructure
                return jasmineStructure.findTestElementPath(textRange)
            }
        }
    }

    private fun getContainingJsFile(element: PsiElement): JSFile? {
        val containingFile = element.containingFile
        return if (containingFile is JSFile) containingFile else null
    }

    private fun buildDirectorySettings(
        psiDirectory: PsiDirectory,
        context: Context
    ): ExtendedSettings? {
        val contextFile = context.file
        val psiProjectDirectory = psiDirectory.project
        Intrinsics.checkNotNullExpressionValue(psiProjectDirectory, "getProject(...)")
        return if (contextFile != psiProjectDirectory.guessProjectDir() && findDefaultConfigFile(context.file) == null) {
            if (JsTestFileByTestNameIndex.hasJasmineTestsUnderDirectory(
                    psiDirectory.project,
                    context.file
                )
            ) ExtendedSettings(
                this,
                context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
                        Intrinsics.checkNotNullParameter(it, "it")
                        it.kind(JsTestRunScopeKind.DIRECTORY)
                        val contextFilePath = context.file.path
                        Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
                        it.testDirectoryPath(contextFilePath)
                    }.build(),
                context.file,
                psiDirectory as PsiElement
            ) else null
        } else {
            ExtendedSettings(
                this,
                context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
                    Intrinsics.checkNotNullParameter(it, "it")
                    it.kind(JsTestRunScopeKind.ALL)
                }.build(),
                context.file,
                psiDirectory as PsiElement
            )
        }
    }

    private fun fixWorkingDir(
        settings: JestRunSettings,
        contextFileOrDir: VirtualFile,
        project: Project
    ): JestRunSettings {
        if (settings.workingDirSystemDependentPath.isEmpty()) {
            val guessedDir = this.guessWorkingDir(project, contextFileOrDir)
            if (guessedDir != null) {
                val jestRunSettingsBuilder: JestRunSettings.Builder = settings.builder()
                Intrinsics.checkNotNullExpressionValue(guessedDir.path, "getPath(...)")
                return jestRunSettingsBuilder.setWorkingDir(guessedDir.path).build()
            }
        }

        return settings
    }

    private inner class ExtendedSettings(
        private val mine: CompiledJestRunConfigurationProducer,
        initialSettings: JestRunSettings,
        contextFileOrDir: VirtualFile,
        @field:NotNull private val enclosingElement: PsiElement
    ) {
        @NotNull
        private val settings: JestRunSettings

        init {
            val project = enclosingElement.project
            settings = mine.fixWorkingDir(initialSettings, contextFileOrDir, project).modify {
                it.scope(it.scope.normalize())
            }
        }

        @NotNull
        fun getEnclosingElement(): PsiElement {
            return enclosingElement
        }

        @NotNull
        fun getSettings(): JestRunSettings {
            return settings
        }
    }

    private inner class Context(
        element: PsiElement,
        @Nullable psiFile: PsiFile,
        file: VirtualFile,
        templateRunSettings: JestRunSettings
    ) {
        val element: PsiElement

        @get:Nullable
        @Nullable
        val psiFile: PsiFile
        val file: VirtualFile
        val templateRunSettings: JestRunSettings

        init {
            Intrinsics.checkNotNullParameter(element, "element")
            Intrinsics.checkNotNullParameter(file, "file")
            Intrinsics.checkNotNullParameter(templateRunSettings, "templateRunSettings")
            this.element = element
            this.psiFile = psiFile
            this.file = file
            this.templateRunSettings = templateRunSettings
        }
    }

}