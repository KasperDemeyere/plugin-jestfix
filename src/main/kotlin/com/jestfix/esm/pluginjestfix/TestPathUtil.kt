package com.jestfix.esm.pluginjestfix

object TestPathUtil {
    /**
     * Replace the /src directory with the /dist directory and the .ts extension with .js in the provided path.
     */
    fun replaceWithCompiledPath(testFilePath: String): String {
        val distFilePath = testFilePath.replace(Regex("/src/"), "/dist/")
        val jsDistFilePath = distFilePath.replace(Regex("ts$"), "js")
        return jsDistFilePath
    }

    /**
     * Chose unit or int config file based on test file extension.
     */
    fun getConfigFile(workingDir: String, testFilePath: String): String {
        return if (testFilePath.endsWith("int.spec.ts")) {
            "${workingDir}/jest.integration.build.config.js"
        } else {
            "${workingDir}/jest.build.config.js"
        }
    }
}