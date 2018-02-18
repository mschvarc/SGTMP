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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import testframework.testplatform.Constants;
import testframework.testplatform.PlatformConfiguration;
import testframework.testplatform.configurationgenerator.PermutationGenerator;
import testframework.testplatform.configurationgenerator.RuntimeConfigurationGenerator;
import testframework.testplatform.configurationgenerator.RuntimeConfigurationGeneratorImpl;
import testframework.testplatform.configurationgenerator.TemplateProcessor;
import testframework.testplatform.xml.GlobalConfigReader;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.TestTemplateParameter;
import testframework.testplatform.dal.entities.TestTemplateValue;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testframework.testplatform.configurationgenerator.Helpers.ensureDirectoryExists;
import static testframework.testplatform.configurationgenerator.Helpers.pathsArrayToString;
import static testframework.testplatform.test.TestRunFilePaths.generatePathsForConfig;
import static testframework.testplatform.test.TestRunFilePaths.generatePathsForConfigInput;
import static testframework.testplatform.test.TestRunFilePaths.generatePathsForConfigOutput;

/**
 * Allows creation of TestRuns from a Test
 * Takes care of generating the required configuration files so a TestRun can be immediately started
 */
@Service
@Scope(value = "prototype")
public class TestRunEnqueuer {

    private static final Logger logger = Logger.getLogger(TestRunEnqueuer.class);

    private final TestRepository testRepository;
    private final TestRunRepository testRunRepository;
    private final Path globalConfigPath;
    private final Path testsDirectory;
    private final GlobalConfigReader globalConfigReader;
    private final TemplateProcessor processor;
    private final Clock clock;

    @Autowired
    public TestRunEnqueuer(TestRepository testRepository,
                           TestRunRepository testRunRepository,
                           TemplateProcessor processor,
                           Clock clock,
                           PlatformConfiguration platformConfiguration,
                           GlobalConfigReader globalConfigReader) {
        this.testRepository = testRepository;
        this.globalConfigPath = Paths.get(platformConfiguration.getGlobalConfigPath());
        this.testsDirectory = Paths.get(platformConfiguration.getTestsDirectory());
        this.testRunRepository = testRunRepository;
        this.processor = processor;
        this.clock = clock;
        this.globalConfigReader = globalConfigReader;
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public void enqueueTest(long testId) throws IOException {
        final Test test = testRepository.getById(testId);
        if (test == null) {
            throw new IllegalArgumentException("test not found");
        }
        final List<List<TestTemplateValue>> allValues = new ArrayList<>();
        for (TestTemplateParameter testTemplateParameter : test.getTemplateValues()) {
            allValues.add(testTemplateParameter.allValues());
        }

        PermutationGenerator<TestTemplateValue> permutationGenerator = new PermutationGenerator<>(allValues);
        for (int i = 0; i < permutationGenerator.size(); i++) {
            final List<TestTemplateValue> actualValues = permutationGenerator.get(i);
            generateTestRun(test, actualValues);
        }
        logger.info("Generated TestRuns from Test");
    }

    private void generateTestRun(Test test, List<TestTemplateValue> templateValues) throws IOException {
        TestRun testRun = new TestRun();
        testRun.setTestRunStatus(TestRunStatus.CREATED);
        testRun.setTest(test);
        testRun.setStartDate(LocalDateTime.now(clock));
        testRun.setMappedValues(templateValues);
        testRunRepository.create(testRun);

        TestRunFilePaths testRunFilePaths = new TestRunFilePaths(testRun, testsDirectory);

        Path testsGeneratedDirectory = testRunFilePaths.getTestsGeneratedDirectory();
        Path testsTemplatesDirectory = testRunFilePaths.getTestsTemplatesDirectory();
        Path testRunDirectory = testRunFilePaths.getTestRunDirectory();
        Path testsUserDirectory = testRunFilePaths.getTestsUserDirectory();
        ensureDirectoryExists(testsGeneratedDirectory);
        ensureDirectoryExists(testsTemplatesDirectory);
        ensureDirectoryExists(testRunDirectory);

        runTemplatingEngine(testsUserDirectory, testsTemplatesDirectory, testRunDirectory, testRun);
        runConfigurationGenerator(testRun, testsGeneratedDirectory, testsTemplatesDirectory);
    }

    private void runTemplatingEngine(Path testsUserDirectory,
                                     Path testsTemplatesDirectory,
                                     Path testRunDirectory,
                                     TestRun testRun) {

        Map<String, String> injectedValues = new HashMap<>();
        injectedValues.put(Constants.TEST_EVAL_INJECTED_PATH, testRunDirectory.resolve(Constants.TESTRESULT_XML).toAbsolutePath().toString());
        injectedValues.put(Constants.GLOBAL_TEST_EVAL_PERMANENT_FAILURE_ENTRY, Boolean.toString(testRun.getTest().isOneStepPermanentFailure()));
        injectedValues.put(Constants.TEST_RESULT_NAME, testRun.getTest().getTestName());

        Map<String, String> templateValues = new HashMap<>();
        for (Map.Entry<TestTemplateParameter, TestTemplateValue> entry : testRun.getMappedValues().entrySet()) {
            templateValues.put(entry.getKey().getKey(), entry.getValue().getSerializedValue());
        }

        Map<String, String> globalConfigVariables = globalConfigReader.getKeyValues(globalConfigPath); //add global_config.xml variables

        injectedValues.putAll(globalConfigVariables);
        injectedValues.putAll(templateValues);
        processor.replaceTemplates(
                generatePathsForConfigInput(testsUserDirectory),
                generatePathsForConfigOutput(testsTemplatesDirectory),
                injectedValues);
    }

    private void runConfigurationGenerator(TestRun testRun, Path testsGeneratedDirectory, Path testsTemplatesDirectory) {
        final Path[] testConfigFilePaths = generatePathsForConfig(testsTemplatesDirectory, testsGeneratedDirectory);
        RuntimeConfigurationGenerator generator = new RuntimeConfigurationGeneratorImpl(testRun.getTest());
        generator.createTestDataModels(pathsArrayToString(testConfigFilePaths));
    }


}
