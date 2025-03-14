package com.jestfix.esm.pluginjestfix

import com.amazon.ion.shaded_.do_not_use.kotlin.jvm.internal.Intrinsics
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.javascript.jest.JestRunConfigurationProducer


class CompiledJestRunConfigurationProducer: JestRunConfigurationProducer() {
    override fun getConfigurationFactory(): ConfigurationFactory {
        val configurationType: CompiledJestConfigurationType = getInstance()
        Intrinsics.checkNotNullExpressionValue(configurationType, "getInstance(...)")
        return configurationType
    }

//    fun setupConfigurationFromCompatibleContext(
//        configuration: CompiledJestRunConfiguration,
//        context: ConfigurationContext,
//        sourceElement: Ref<PsiElement>
//    ): Boolean {
//        Intrinsics.checkNotNullParameter(configuration, "configuration")
//        Intrinsics.checkNotNullParameter(context, "context")
//        Intrinsics.checkNotNullParameter(sourceElement, "sourceElement")
//        val element = context.psiLocation
//        if (element != null && this.isTestRunnerAvailableFor(element, context)) {
//            val jestRunSettings = configuration.runSettings
//            Intrinsics.checkNotNullExpressionValue(jestRunSettings, "getRunSettings(...)")
//            val extendedSettings = this.buildContextSettings(element, jestRunSettings)
//            if (extendedSettings == null) {
//                return false
//            } else {
//                configuration.runSettings = extendedSettings.getSettings()
//                sourceElement.set(extendedSettings.getEnclosingElement())
//                configuration.setGeneratedName()
//                return true
//            }
//        } else {
//            return false
//        }
//    }
//
//    fun isConfigurationFromCompatibleContext(
//        configuration: CompiledJestRunConfiguration,
//        context: ConfigurationContext
//    ): Boolean {
//        Intrinsics.checkNotNullParameter(configuration, "configuration")
//        Intrinsics.checkNotNullParameter(context, "context")
//        val element = context.psiLocation
//        if (element == null) {
//            return false
//        } else {
//            val runConfiguration: RunConfiguration = cloneTemplateConfiguration(context).configuration
//            if (runConfiguration !is CompiledJestRunConfiguration) {
//                return false
//            } else {
//                val jestRunSettings: JestRunSettings = runConfiguration.runSettings
//                Intrinsics.checkNotNullExpressionValue(jestRunSettings, "getRunSettings(...)")
//                val extendedSettings: ExtendedSettings? = this.buildContextSettings(element, jestRunSettings)
//                if (extendedSettings == null) {
//                    return false
//                } else {
//                    val thisRunSettings = extendedSettings.getSettings()
//                    val configurationRunSettings: JestRunSettings = configuration.runSettings
//                    Intrinsics.checkNotNullExpressionValue(configurationRunSettings, "getRunSettings(...)")
//                    return this.areConfigFilesEffectivelySame(thisRunSettings, configurationRunSettings) &&
//                            thisRunSettings.workingDirSystemDependentPath == configurationRunSettings.workingDirSystemDependentPath &&
//                            thisRunSettings.scope.normalize() == configurationRunSettings.scope.normalize()
//                }
//            }
//        }
//    }
//
//    private fun areConfigFilesEffectivelySame(
//        generatedSettings: JestRunSettings,
//        existingSettings: JestRunSettings
//    ): Boolean {
//        return generatedSettings.configFileSystemDependentPath == existingSettings.configFileSystemDependentPath ||
//                (generatedSettings.configFileSystemDependentPath.isEmpty() && (existingSettings.configFileSystemDependentPath).isNotEmpty())
//    }
//
//
//    private fun buildContextSettings(element: PsiElement, templateRunSettings: JestRunSettings): ExtendedSettings?  {
//        val psiFile: PsiFile = element.containingFile
//        val virtualFile: VirtualFile? = PsiUtilCore.getVirtualFile(element)
//        if (virtualFile == null) {
//            return null
//        } else {
//            val context = Context(element, psiFile, virtualFile, templateRunSettings)
//            if (element is PsiDirectory) {
//                return this.buildDirectorySettings(element, context)
//            } else {
//                var extendedSettings: ExtendedSettings? = this.buildSuiteOrTestSettings(context)
//                return if (extendedSettings != null) {
//                    extendedSettings
//                } else {
//                    extendedSettings = this.buildConfigSettings(context)
//                    extendedSettings ?: this.buildTestFileSettings(context)
//                }
//            }
//        }
//    }
//
//    private fun buildTestFileSettings(context: Context): ExtendedSettings? {
//        val psiFile = context.psiFile
//        val testFileType = if (psiFile is JSFile) psiFile.testFileType else null
//        if (testFileType == JSTestFileType.JASMINE) {
//            val builder: JestRunSettings.Builder = context.templateRunSettings.builder()
//                .scope { it: JsTestRunScope.Builder ->
//                    Intrinsics.checkNotNullParameter(it, "it")
//                    it.kind(JsTestRunScopeKind.TEST_FILE)
//                    val contextFilePath = context.file.path
//                    Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
//                    it.testFilePath(contextFilePath)
//                }
//            return ExtendedSettings(
//                this,
//                builder.build(),
//                context.file,
//                context.psiFile
//            )
//        } else {
//            return null
//        }
//    }
//
//    private fun buildConfigSettings(context: Context): ExtendedSettings? {
//        if (JestUtil.isJestConfigFile(context.file.name as CharSequence)) {
//            val settings = context.templateRunSettings.builder()
//                .scope { it: JsTestRunScope.Builder ->
//                    Intrinsics.checkNotNullParameter(it, "it")
//                    it.kind = JsTestRunScopeKind.ALL
//                }
//                .build()
//            return ExtendedSettings(
//                this,
//                settings,
//                context.file,
//                context.psiFile
//            )
//        } else {
//            if (PackageJsonUtil.isPackageJsonFile(context.psiFile)) {
//                val jestProp: JsonProperty? = PackageJsonUtil.findContainingTopLevelProperty(context.element)
//                if (jestProp != null && "jest" == jestProp.name) {
//                    val settings = context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
//                        Intrinsics.checkNotNullParameter(it, "it")
//                        it.kind = JsTestRunScopeKind.ALL
//                    }.build()
//                    return ExtendedSettings(
//                        this,
//                        settings,
//                        context.file,
//                        jestProp as PsiElement
//                    )
//                }
//            }
//
//            return null
//        }
//    }
//
//    private fun buildSuiteOrTestSettings(context: Context): ExtendedSettings? {
//        val testPath = this.findContextSuiteOrTestPath(context.element)
//        if (testPath == null) {
//            return null
//        } else {
//            val jestRunSettingsBuilder: JestRunSettings.Builder = context.templateRunSettings.builder()
//            var jsTestRunScopeBuilder: JsTestRunScope.Builder = jestRunSettingsBuilder.scope.builder()
//            val contextFilePath = context.file.path
//            Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
//            jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testFilePath(contextFilePath).build())
//            val testName = testPath.testName
//            val testNames: MutableList<String>
//            if (testName == null) {
//                jsTestRunScopeBuilder = jestRunSettingsBuilder.scope.builder().kind(JsTestRunScopeKind.SUITE)
//                testNames = testPath.suiteNames
//                Intrinsics.checkNotNullExpressionValue(testNames, "getSuiteNames(...)")
//                jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testNames(testNames).build())
//            } else {
//                jsTestRunScopeBuilder = jestRunSettingsBuilder.scope.builder().kind(JsTestRunScopeKind.TEST)
//                testNames = testPath.suiteNames
//                Intrinsics.checkNotNullExpressionValue(testNames, "getSuiteNames(...)")
//                testNames.add(testName)
//                jestRunSettingsBuilder.scope(jsTestRunScopeBuilder.testNames(testNames).build())
//            }
//
//            val jestRunSettings: JestRunSettings = jestRunSettingsBuilder.build()
//            val testElementPath = testPath.testElement
//            Intrinsics.checkNotNullExpressionValue(testElementPath, "getTestElement(...)")
//            return ExtendedSettings(this, jestRunSettings, context.file, testElementPath)
//        }
//    }
//
//    private fun findContextSuiteOrTestPath(element: PsiElement): JsTestElementPath? {
//        if (element is PsiFileSystemItem) {
//            return null
//        } else {
//            val containingJsFile = this.getContainingJsFile(element)
//            if (containingJsFile == null) {
//                return null
//            } else {
//                val jsFile: JSFile = containingJsFile
//                val textRange: TextRange = element.textRange
//                val testFileStructure: AbstractTestFileStructure = JasmineFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile)
//                Intrinsics.checkNotNullExpressionValue(testFileStructure, "fetchCachedTestFileStructure(...)")
//                val jasmineStructure = testFileStructure as JasmineFileStructure
//                return jasmineStructure.findTestElementPath(textRange)
//            }
//        }
//    }
//
//    private fun getContainingJsFile(element: PsiElement): JSFile? {
//        val containingFile = element.containingFile
//        return if (containingFile is JSFile) containingFile else null
//    }
//
//    private fun buildDirectorySettings(
//        psiDirectory: PsiDirectory,
//        context: Context
//    ): ExtendedSettings? {
//        val contextFile = context.file
//        val psiProjectDirectory = psiDirectory.project
//        Intrinsics.checkNotNullExpressionValue(psiProjectDirectory, "getProject(...)")
//        return if (contextFile != psiProjectDirectory.guessProjectDir() && findDefaultConfigFile(context.file) == null) {
//            if (JsTestFileByTestNameIndex.hasJasmineTestsUnderDirectory(
//                    psiDirectory.project,
//                    context.file
//                )
//            ) ExtendedSettings(
//                this,
//                context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
//                        Intrinsics.checkNotNullParameter(it, "it")
//                        it.kind(JsTestRunScopeKind.DIRECTORY)
//                        val contextFilePath = context.file.path
//                        Intrinsics.checkNotNullExpressionValue(contextFilePath, "getPath(...)")
//                        it.testDirectoryPath(contextFilePath)
//                    }.build(),
//                context.file,
//                psiDirectory as PsiElement
//            ) else null
//        } else {
//            ExtendedSettings(
//                this,
//                context.templateRunSettings.builder().scope { it: JsTestRunScope.Builder ->
//                    Intrinsics.checkNotNullParameter(it, "it")
//                    it.kind(JsTestRunScopeKind.ALL)
//                }.build(),
//                context.file,
//                psiDirectory as PsiElement
//            )
//        }
//    }
//
//    private fun fixWorkingDir(
//        settings: JestRunSettings,
//        contextFileOrDir: VirtualFile,
//        project: Project
//    ): JestRunSettings {
//        if (settings.workingDirSystemDependentPath.isEmpty()) {
//            val guessedDir = this.guessWorkingDir(project, contextFileOrDir)
//            if (guessedDir != null) {
//                val jestRunSettingsBuilder: JestRunSettings.Builder = settings.builder()
//                Intrinsics.checkNotNullExpressionValue(guessedDir.path, "getPath(...)")
//                return jestRunSettingsBuilder.setWorkingDir(guessedDir.path).build()
//            }
//        }
//
//        return settings
//    }
//
////    override fun isConfigurationFromContext(runConfig: CompiledJestRunConfiguration, context: ConfigurationContext): Boolean {
////        TODO("Not yet implemented")
////    }
//
//    private inner class ExtendedSettings(
//        private val mine: CompiledJestRunConfigurationProducer,
//        initialSettings: JestRunSettings,
//        contextFileOrDir: VirtualFile,
//        @field:NotNull private val enclosingElement: PsiElement
//    ) {
//        @NotNull
//        private val settings: JestRunSettings
//
//        init {
//            val project = enclosingElement.project
//            settings = mine.fixWorkingDir(initialSettings, contextFileOrDir, project).modify { it.scope(it.scope.normalize()) }
//        }
//
//        @NotNull
//        fun getEnclosingElement(): PsiElement {
//            return enclosingElement
//        }
//
//        @NotNull
//        fun getSettings(): JestRunSettings {
//            return settings
//        }
//    }
//
//    private inner class Context(
//        element: PsiElement,
//        @Nullable psiFile: PsiFile,
//        file: VirtualFile,
//        templateRunSettings: JestRunSettings
//    ) {
//        val element: PsiElement
//
//        @get:Nullable
//        @Nullable
//        val psiFile: PsiFile
//        val file: VirtualFile
//        val templateRunSettings: JestRunSettings
//
//        init {
//            Intrinsics.checkNotNullParameter(element, "element")
//            Intrinsics.checkNotNullParameter(file, "file")
//            Intrinsics.checkNotNullParameter(templateRunSettings, "templateRunSettings")
//            this.element = element
//            this.psiFile = psiFile
//            this.file = file
//            this.templateRunSettings = templateRunSettings
//        }
//    }

}