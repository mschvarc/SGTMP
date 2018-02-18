/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.testplatform.test;

import testframework.testplatform.Constants;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestRun;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TestRunFilePaths {
    private static final String FS = File.separator;
    private Path testsUserDirectory;
    private Path testsGeneratedDirectory;
    private Path testsTemplatesDirectory;
    private Path testRunDirectory;

    TestRunFilePaths(TestRun testRun, Path testsDirectory) {
        Test test = testRun.getTest();
        testsUserDirectory = testsDirectory.resolve(test.getUuid() + FS + "config" + FS + "user");
        testsGeneratedDirectory = testsDirectory.resolve(test.getUuid() + FS + "testruns" + FS + testRun.getId() + FS + "generated");
        testsTemplatesDirectory = testsDirectory.resolve(test.getUuid() + FS + "testruns" + FS + testRun.getId() + FS + "templated");
        testRunDirectory = testsDirectory.resolve(test.getUuid() + FS + "testruns" + FS + testRun.getId());
    }

    public static Path[] generatePathsForConfig(Path input, Path output) {
        ArrayList<Path> paths = new ArrayList<>();
        paths.addAll(simulationPaths(input));
        paths.addAll(simulationPaths(output));
        return paths.toArray(new Path[paths.size()]);
    }

    public static Path[] generatePathsForConfigInput(Path input) {
        ArrayList<Path> paths = new ArrayList<>();
        paths.addAll(simulationPaths(input));
        return paths.toArray(new Path[paths.size()]);
    }

    public static Path[] generatePathsForConfigOutput(Path output) {
        ArrayList<Path> paths = new ArrayList<>();
        paths.addAll(simulationPaths(output));
        return paths.toArray(new Path[paths.size()]);
    }

    public static List<Path> simulationPaths(Path basePath) {
        return Arrays.asList(
                basePath.resolve(Constants.SIMULATION_CONFIG_XML).toAbsolutePath(),
                basePath.resolve(Constants.SIMULATORS_XML).toAbsolutePath(),
                basePath.resolve(Constants.MODELS_XML).toAbsolutePath(),
                basePath.resolve(Constants.WIRES_XML).toAbsolutePath(),
                basePath.resolve(Constants.CONFIG_XML).toAbsolutePath()
        );
    }

    Path getTestsUserDirectory() {
        return testsUserDirectory;
    }

    Path getTestsGeneratedDirectory() {
        return testsGeneratedDirectory;
    }

    Path getTestsTemplatesDirectory() {
        return testsTemplatesDirectory;
    }

    Path getTestRunDirectory() {
        return testRunDirectory;
    }

}
