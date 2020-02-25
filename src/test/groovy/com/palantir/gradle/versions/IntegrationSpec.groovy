/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.gradle.versions


import groovy.transform.CompileStatic
import nebula.test.dependencies.DependencyGraph
import nebula.test.dependencies.GradleDependencyGenerator
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir

@CompileStatic
@ExtendWith(ParameterizedClass)
class IntegrationSpec extends AbstractIntegrationTestKit {

    @TempDir
    public File gradleTestKitDir

    @ParameterizedClass.Parameter(0)
    public String gradleVersion

    boolean ignoreDeprecations

    @ParameterizedClass.Parameters(name = "Gradle {0}")
    static Object[][] data() {
        return GradleTestVersions.GRADLE_VERSIONS.stream().map { [it] as Object[] }.toArray() as Object[][]
    }

    @BeforeEach
    final void beforeEachMain(TestInfo testInfo) {
        def testName = (testInfo.displayName + " " + testInfo.testMethod.get().name)
                .replace('()', "")
        super.initialize(testInfo.testClass.get(), testName)

        super.gradleVersion = gradleVersion

        keepFiles = true
        // testKit.debug = true // TODO cannot do this when running in parallel
        // Ends up in: org.gradle.testkit.runner.internal.ToolingApiGradleExecutor.buildConnector -> embedded
    }

    @AfterEach
    final void afterEachMain() {
        cleanup()
    }

    @Override
    GradleRunner createRunner(String... tasks) {
        // Very important: need each test kit to use a different location
        // Currently it sets it to a static location via org.gradle.testkit.runner.internal.DefaultGradleRunner.calculateTestKitDirProvider
        return super.createRunner(tasks).withTestKitDir(gradleTestKitDir)
    }

    @Override
    List<String> calculateArguments(String... args) {
        // Groovy doesn't let me pass varargs directly
        def list = GroovySucksHelper.callVarags({ super.calculateArguments(it) }, args)
        // This way we can control this value even with parallelism
        if (ignoreDeprecations) {
            list.push("--warning-mode=none")
        } else {
            list.push("--warning-mode=all")
        }
        return list
    }

    @CompileStatic
    protected File generateMavenRepo(String... graph) {
        DependencyGraph dependencyGraph = new DependencyGraph(graph)
        GradleDependencyGenerator generator = new GradleDependencyGenerator(
                dependencyGraph, new File(projectDir, "build/testrepogen").toString())
        // TODO this does not work in parallel.
        // return generator.generateTestMavenRepo()
        GradleRunner.create()
                .withProjectDir(generator.gradleRoot)
                .withTestKitDir(gradleTestKitDir)
                .withArguments("publishMavenPublicationToMavenRepository")
                .build()
        return generator.mavenRepoDir
    }
}
