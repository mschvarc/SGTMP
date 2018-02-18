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
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.MeasureType;
import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;
import testframework.testplatform.dal.entities.measure.operators.LessThanOperand;
import testframework.testplatform.dal.repository.ExpectedResultRepository;
import testframework.testplatform.dal.repository.IncidentRepository;
import testframework.testplatform.dal.repository.MeasureAttributeRepository;
import testframework.testplatform.dal.repository.MeasureRepository;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;
import testframework.testplatform.dal.repository.TestStepRepository;
import testframework.testplatform.dal.repository.TestTemplateParameterRepository;
import testframework.testplatform.dal.repository.TestTemplateValuesRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class ArduinoTestLauncher {

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


    @PersistenceContext
    private EntityManager manager;

    private static Date date(long stamp) {
        Date date = new Date();
        date.setTime(stamp);
        return date;
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

        test.setTestDescription("test description");
        test.setTestName("test name");
        test.setUuid(UUID.fromString("0313c600-b72d-11e7-abc4-cec278b6b50a").toString());

        testRepository.create(test);


        testRepository.update(test);

        //setup measures for testing
        MeasureAttribute measureAttribute1 = new MeasureAttribute();
        measureAttribute1.setSimulatorId("SimulatorArduino");
        measureAttribute1.setModelId("Model_Arduino");
        measureAttribute1.setName("SimulatorArduino:Model_Arduino:voltageOut");
        measureAttribute1.setMeasureInputName("voltageMeasure");

        Measure measure1 = new Measure();
        measure1.setMeasureType(MeasureType.EXPECTED);
        measure1.setValue(50);
        measure1.setUnit("mili-Volts");
        measure1.setMeasureAttribute(measureAttribute1);
        measureRepository.create(Collections.singletonList(measure1));

        LessThanOperand lessThanOperand = new LessThanOperand(new Measure(5000));
        Map<MeasureAttribute, ExpectedResult> expectedResultMap = new HashMap<>();
        expectedResultMap.put(measureAttribute1, lessThanOperand);

        TestStep step1 = new TestStep();
        step1.setExpectedResults(expectedResultMap);
        step1.setStep(5);
        step1.setTest(test);

        test.setTestSteps(Collections.singletonList(step1));

        createTestLauncher();
        createTestRunLauncher();

        System.out.println("PATH>>: " + Paths.get(".").toAbsolutePath().normalize().toString());
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
                globalConfigReader
        );
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
        List<Incident> incidents = incidentRepository.getAllIncidentsForTestRun(startedTestRun);
        if (!incidents.isEmpty()) {
            for (Incident incident : incidents) {
                System.out.println(incident.getDescription());
            }
        }

        Assert.assertEquals(TestRunEvaluation.SUCCESS, testRun.getEvaluation());
        Assert.assertEquals(test, testRun.getTest());
    }

}
