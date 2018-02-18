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

package testframework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testframework.testplatform.PlatformConfiguration;
import testframework.testplatform.configurationgenerator.TemplateProcessor;
import testframework.testplatform.launcher.ProcessLauncher;
import testframework.testplatform.selection.TestRunSelectionStrategy;
import testframework.testplatform.test.TestRunEnqueuer;
import testframework.testplatform.test.TestRunner;
import testframework.testplatform.xml.GlobalConfigReader;
import testframework.testplatform.dal.entities.Incident;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunEvaluation;
import testframework.testplatform.dal.entities.TestStep;
import testframework.testplatform.dal.entities.TestTemplateParameter;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.MeasureType;
import testframework.testplatform.dal.entities.measure.operators.AndOperand;
import testframework.testplatform.dal.entities.measure.operators.GreaterThanOperand;
import testframework.testplatform.dal.entities.measure.operators.LessThanOperand;
import testframework.testplatform.dal.entities.measure.operators.OrOperand;
import testframework.testplatform.dal.repository.ExpectedResultRepository;
import testframework.testplatform.dal.repository.IncidentRepository;
import testframework.testplatform.dal.repository.MeasureAttributeRepository;
import testframework.testplatform.dal.repository.MeasureRepository;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;
import testframework.testplatform.dal.repository.TestStepRepository;
import testframework.testplatform.dal.repository.TestTemplateParameterRepository;
import testframework.testplatform.dal.repository.TestTemplateValuesRepository;

import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
public class TestLauncher {

    private final Test test = new Test();

    @Autowired
    TestRepository testRepository;
    @Autowired
    TestRunRepository testRunRepository;
    @Autowired
    TestTemplateValuesRepository testTemplateValuesRepository;
    @Autowired
    TestTemplateParameterRepository testTemplateParametersRepository;
    @Autowired
    IncidentRepository incidentRepository;
    @Autowired
    MeasureRepository measureRepository;
    @Autowired
    MeasureAttributeRepository measureAttributeRepository;
    @Autowired
    ExpectedResultRepository expectedResultRepository;
    @Autowired
    TemplateProcessor processor;
    @Autowired
    TestRunSelectionStrategy testRunSelectionStrategy;
    @Autowired
    TestStepRepository testStepRepository;
    @Autowired
    ProcessLauncher processLauncher;
    @Autowired
    ObjectMapper mapper;
    @Value(value = "${testlauncher_mosaikEnvDir}")
    String mosaikEnvDir;
    @Value(value = "${testlauncher_pythonPlatformFile}")
    String pythonPlatformFile;
    private GlobalConfigReader globalConfigReader;

    private PlatformConfiguration platformConfiguration;
    private TestRunEnqueuer testLauncher;
    private TestRunner testRunService;
    private Clock clock = Clock.fixed(Instant.ofEpochSecond(1508247953L), ZoneId.of("UTC"));


    private void setupMeasures(Test test) {

        {
            MeasureAttribute measureAttribute1 = new MeasureAttribute();
            measureAttribute1.setSimulatorId("Simulator1");
            measureAttribute1.setModelId("Model1");
            measureAttribute1.setName("Simulator1:Model1:measureValue1");
            measureAttribute1.setMeasureInputName("name1");

            Measure ten = new Measure(10);
            ten.setMeasureAttribute(measureAttribute1);
            ten.setMeasureType(MeasureType.EXPECTED);
            Measure twenty = new Measure(20);
            twenty.setMeasureAttribute(measureAttribute1);
            twenty.setMeasureType(MeasureType.EXPECTED);
            Measure fifty = new Measure(50);
            fifty.setMeasureAttribute(measureAttribute1);
            fifty.setMeasureType(MeasureType.EXPECTED);
            Measure hundred = new Measure(100);
            hundred.setMeasureAttribute(measureAttribute1);
            hundred.setMeasureType(MeasureType.EXPECTED);
            Measure thousand = new Measure(1000);
            thousand.setMeasureAttribute(measureAttribute1);
            thousand.setMeasureType(MeasureType.EXPECTED);

            GreaterThanOperand greaterThanTwenty = new GreaterThanOperand(twenty); //x > 20
            LessThanOperand lessThanHundred = new LessThanOperand(hundred); // x < 100
            AndOperand andOperand = new AndOperand();
            andOperand.add(greaterThanTwenty);
            andOperand.add(lessThanHundred);

            TestStep testStep = new TestStep();
            testStep.setStep(50);
            testStep.setTest(test);
            testStep.getExpectedResults().put(measureAttribute1, andOperand);
            test.getTestSteps().add(testStep);
            expectedResultRepository.create(andOperand);
            testStepRepository.create(testStep);

            //persist
            testRepository.update(test);
        }

        {
            MeasureAttribute measureAttribute2 = new MeasureAttribute();
            measureAttribute2.setSimulatorId("Simulator1");
            measureAttribute2.setModelId("Model1");
            measureAttribute2.setName("Simulator1:Model1:measureValue2");
            measureAttribute2.setMeasureInputName("name2");

            Measure ten = new Measure(10);
            ten.setMeasureType(MeasureType.EXPECTED);
            ten.setMeasureAttribute(measureAttribute2);
            Measure twenty = new Measure(20);
            twenty.setMeasureType(MeasureType.EXPECTED);
            twenty.setMeasureAttribute(measureAttribute2);
            Measure fifty = new Measure(50);
            fifty.setMeasureType(MeasureType.EXPECTED);
            fifty.setMeasureAttribute(measureAttribute2);
            Measure hundred = new Measure(100);
            hundred.setMeasureType(MeasureType.EXPECTED);
            hundred.setMeasureAttribute(measureAttribute2);
            Measure thousand = new Measure(1000);
            thousand.setMeasureType(MeasureType.EXPECTED);
            thousand.setMeasureAttribute(measureAttribute2);

            GreaterThanOperand greaterThanHundred = new GreaterThanOperand(hundred); //x>100
            LessThanOperand lessThanTwenty = new LessThanOperand(twenty); //x<20
            OrOperand orOperand = new OrOperand();
            orOperand.add(greaterThanHundred);
            orOperand.add(lessThanTwenty);

            expectedResultRepository.create(orOperand);
            TestStep testStep = new TestStep();
            testStep.setStep(100);
            testStep.setTest(test);
            testStep.getExpectedResults().put(measureAttribute2, orOperand);
            expectedResultRepository.create(orOperand);
            testStepRepository.create(testStep);
            test.getTestSteps().add(testStep);

            //persist
            testRepository.update(test);
        }

    }

    @org.junit.Before
    public void setup() throws Exception {

        platformConfiguration = new PlatformConfiguration(
                "src/test/resources/tests/global_config.xml",
                "src/test/resources/tests",
                mosaikEnvDir,
                pythonPlatformFile,
                "true",
                "true");

        globalConfigReader = new GlobalConfigReader(platformConfiguration);


        final TestTemplateParameter testTemplateParameter1 = new TestTemplateParameter();
        testTemplateParameter1.setKey("variableA");
        testTemplateParameter1.setMaximumValue(0L);
        testTemplateParameter1.setMinimumValue(100L);
        testTemplateParameter1.setReplaceMode(TestTemplateParameter.ReplaceMode.TEST_BOUNDARY_INCLUSIVE);
        testTemplateParametersRepository.create(testTemplateParameter1);


        final TestTemplateParameter testTemplateParameter2 = new TestTemplateParameter();
        testTemplateParameter2.setKey("variableB");
        testTemplateParameter2.setLiterals(Arrays.asList("X", "Y"));
        testTemplateParameter2.setReplaceMode(TestTemplateParameter.ReplaceMode.STRING_LITERALS);
        testTemplateParametersRepository.create(testTemplateParameter2);


        test.setTestDescription("test description");
        test.setTestName("test name");
        test.setUuid(UUID.fromString("4f162f92-f7ed-4580-84a0-2fcd739e746b").toString());

        testRepository.create(test);

        setupMeasures(test);


        List<TestTemplateParameter> paramsList = new ArrayList<>();
        paramsList.add(testTemplateParameter1);
        paramsList.add(testTemplateParameter2);
        test.setTemplateValues(paramsList); //uncomment for param testing
        testRepository.update(test);

        createTestLauncher();
        createTestRunLauncher();

    }

    private void createTestRunLauncher() throws Exception {
        testRunService = new TestRunner(testRunRepository,
                incidentRepository,
                measureRepository,
                testRunSelectionStrategy,
                processLauncher,
                clock,
                mapper,
                platformConfiguration);
    }

    private void createTestLauncher() throws Exception {
        testLauncher = new TestRunEnqueuer(
                testRepository,
                testRunRepository,
                processor,
                Clock.systemDefaultZone(),
                platformConfiguration,
                globalConfigReader);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    @org.junit.Test
    public void testRun() throws Exception {

        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        testLauncher.enqueueTest(test.getId());
        List<TestRun> testRuns = testRunService.findStartableTestRuns();
        assertThat(testRuns.size()).isGreaterThan(0);

        TestRun startedTestRun = testRuns.get(0);
        testRunService.startTestRunNonBlocking(startedTestRun.getId());

        testRunService.getTestRunResultBlocking(startedTestRun.getId());

        TestRun testRun = testRunRepository.getById(startedTestRun.getId());
        assertThat(testRun).isNotNull();
        assertThat(testRun.getTest()).isEqualToComparingFieldByField(startedTestRun.getTest());
        List<Incident> incidents = incidentRepository.getAllIncidentsForTestRun(startedTestRun);
        if (!incidents.isEmpty()) {
            for (Incident incident : incidents) {
                System.out.println(incident.getDescription());
            }
        }

        Assert.assertEquals(test, testRun.getTest());
        Assert.assertEquals(TestRunEvaluation.SUCCESS, testRun.getEvaluation());
    }

}
