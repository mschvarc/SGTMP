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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import testframework.testplatform.PlatformConfiguration;
import testframework.testplatform.exceptions.TestRunException;
import testframework.testplatform.launcher.ProcessInfoWrapper;
import testframework.testplatform.launcher.ProcessLauncher;
import testframework.testplatform.selection.TestRunSelectionStrategy;
import testframework.testplatform.xml.GlobalConfigReader;
import testframework.testplatform.xml.TestConfigReader;
import testframework.testplatform.xml.TestRunResultReader;
import testframework.testplatform.dal.entities.Incident;
import testframework.testplatform.dal.entities.ProcessReturnInformation;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunEvaluation;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.repository.IncidentRepository;
import testframework.testplatform.dal.repository.MeasureRepository;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static testframework.testplatform.configurationgenerator.Helpers.pathsArrayToString;

@Service
@Scope(value = "singleton")
public class TestRunner {

    private static final String NL = System.lineSeparator();
    private static final Logger logger = Logger.getLogger(TestRunner.class);
    private static final long RESULT_SIMULATOR_TIMEOUT = 2;
    private static final TimeUnit RESULT_SIMULATOR_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final long MOSAIK_PROCESS_START_DELAY = 1000;
    private final TestRunRepository testRunRepository;
    private final Path testsDirectory;
    private final IncidentRepository incidentRepository;
    private final GlobalConfigReader globalConfigReader;
    private final TestConfigReader testConfigReader = new TestConfigReader();
    private final TestRunResultReader testRunResultReader = new TestRunResultReader();
    private final MeasureRepository measureRepository;
    private final ObjectMapper mapper;
    private final TestRunSelectionStrategy testRunSelectionStrategy;
    private final Clock clock;
    private final Map<Long, ProcessInfoWrapper> runningMosaikWrappers = new ConcurrentHashMap<>();
    private final Map<Long, ProcessInfoWrapper> runningProcessWrappers = new ConcurrentHashMap<>();
    private final ProcessLauncher processLauncher;

    @Autowired
    public TestRunner(TestRunRepository testRunRepository,
                      IncidentRepository incidentRepository,
                      MeasureRepository measureRepository,
                      TestRunSelectionStrategy testRunSelectionStrategy,
                      ProcessLauncher processLauncher,
                      Clock clock,
                      ObjectMapper mapper,
                      PlatformConfiguration configuration) {

        this.testRunSelectionStrategy = testRunSelectionStrategy;
        this.testsDirectory = Paths.get(configuration.getTestsDirectory());
        this.testRunRepository = testRunRepository;
        this.incidentRepository = incidentRepository;
        this.measureRepository = measureRepository;
        this.clock = clock;
        this.processLauncher = processLauncher;
        this.mapper = mapper;
        this.globalConfigReader = new GlobalConfigReader(configuration);
    }


    @Transactional(Transactional.TxType.REQUIRED)
    public List<TestRun> findStartableTestRuns() {
        List<TestRun> testRuns = testRunSelectionStrategy.selectTestRunsToStart();
        if (testRuns.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(testRuns);
    }

    /**
     * Non-blocking, starts a TestRun by its ID
     *
     * @param testRunId test run to start
     */
    @Transactional(Transactional.TxType.REQUIRED)
    public void startTestRunNonBlocking(long testRunId) {
        TestRun testRun = testRunRepository.getById(testRunId);
        if (testRun == null) {
            throw new IllegalArgumentException("testRun is null");
        }
        TestRunFilePaths testRunFilePaths = new TestRunFilePaths(testRun, testsDirectory);

        if (!testRun.getTestRunStatus().isStartable()) {
            throw new IllegalStateException("TestRun state must be startable, is " + testRun.getTestRunStatus());
        }
        testRun.setTestRunStatus(TestRunStatus.STARTED);
        assert !testRun.getTestRunStatus().isStartable();

        //get additional simulators to launch
        Path testConfigPath = testRunFilePaths.getTestsGeneratedDirectory().resolve("config.xml");
        assert testConfigPath.toFile().exists();
        List<List<String>> programArgs = testConfigReader.extractPrograms(testConfigPath);
        boolean programsRedirectErrorStream = testConfigReader.redirectErrorStream(testConfigPath);

        ProcessInfoWrapper startedProcessWrapper = processLauncher.startProcessesNonBlocking(programArgs, programsRedirectErrorStream);
        runningProcessWrappers.put(testRun.getId(), startedProcessWrapper);

        //give processes time to start before launching Mosaik
        //processes(simulators) can be still starting when Mosaik tries to connect
        try {
            Thread.sleep(MOSAIK_PROCESS_START_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //add new file paths to Mosaik launch params array [1 -> 4]
        ProcessInfoWrapper mosaikProcessWrapper = startMosaikProcess(testRunFilePaths.getTestsGeneratedDirectory());
        this.runningMosaikWrappers.put(testRun.getId(), mosaikProcessWrapper);

        testRunRepository.update(testRun);
    }


    /**
     * Retrieve the test run status of entities, blocks until output is received
     *
     * @param testRunId test run to start
     */
    @Transactional
    public void getTestRunResultBlocking(long testRunId) {
        TestRun testRun = testRunRepository.getById(testRunId);
        if (testRun == null) {
            throw new IllegalArgumentException("testRun is null");
        }
        if (testRun.getTestRunStatus() != TestRunStatus.STARTED) {
            throw new IllegalStateException("Attempting to get results of a test not in STARTED state");
        }
        TestRunFilePaths testRunFilePaths = new TestRunFilePaths(testRun, testsDirectory);
        Path testConfigPath = testRunFilePaths.getTestsGeneratedDirectory().resolve("config.xml");

        boolean mosaikPrintResults = globalConfigReader.printResult();
        boolean testsPrintResults = testConfigReader.printResults(testConfigPath);

        List<ProcessReturnInformation> simulationResults = null;
        List<ProcessReturnInformation> mosaikResults = null;

        //BLOCKING SECTION
        boolean success = true;
        try {
            mosaikResults = processLauncher.getResultsBlocking(runningMosaikWrappers.get(testRun.getId()), mosaikPrintResults);
        } catch (TestRunException ex) {
            generateIncident(testRun, ex, "Error during mosaik test result gathering");
            success = false;
            processLauncher.forceCleanup(runningMosaikWrappers.get(testRun.getId()));
        }

        //give processes time to cleanup, fail otherwise
        try {
            simulationResults = processLauncher
                    .getResultsBlocking(runningProcessWrappers.get(testRun.getId()),
                            testsPrintResults,
                            RESULT_SIMULATOR_TIMEOUT,
                            RESULT_SIMULATOR_TIMEOUT_UNIT
                    );
        } catch (TestRunException ex) {
            generateIncident(testRun, ex, "Error during test result gathering");
            success = false;
            processLauncher.forceCleanup(runningProcessWrappers.get(testRun.getId()));
        }
        if (!success) {
            return;
        }

        assert mosaikResults != null && mosaikResults.size() == 1;
        processResults(simulationResults, mosaikResults.get(0), testRun, testRunFilePaths.getTestRunDirectory());

        //cleanup maps
        runningMosaikWrappers.remove(testRun.getId());
        runningProcessWrappers.remove(testRun.getId());

        //update entities
        testRunRepository.update(testRun);
    }


    /**
     * Generates an incident for this test run
     *
     * @param testRun test run which caused an incident
     * @param ex      source exception
     */
    private void generateIncident(TestRun testRun, Exception ex, String message) {
        Incident incident = new Incident();
        incident.setTestRun(testRun);
        incident.setDescription(incident + ex.toString());
        incidentRepository.create(incident);

        testRun.setEvaluation(TestRunEvaluation.OUT_OF_RANGE);
        testRun.setEndDate(LocalDateTime.now(clock));
        testRun.setTestRunStatus(TestRunStatus.FINISHED);
        testRunRepository.update(testRun);

        logger.error(message);
        logger.error("Exception during test run", ex);
    }


    private ProcessInfoWrapper startMosaikProcess(Path testsGeneratedDirectory) {
        final Path[] mosaikInputArgs = new Path[]{
                testsGeneratedDirectory.resolve("simulation_config.xml").toAbsolutePath(),
                testsGeneratedDirectory.resolve("simulators.xml").toAbsolutePath(),
                testsGeneratedDirectory.resolve("models.xml").toAbsolutePath(),
                testsGeneratedDirectory.resolve("wires.xml").toAbsolutePath()
        };
        List<String> mosaikLaunchArgs = new ArrayList<>(globalConfigReader.extractMosaikConfig());
        boolean mosaikRedirectErrorStream = globalConfigReader.redirectErrorStream();
        mosaikLaunchArgs.addAll(Arrays.asList(pathsArrayToString(mosaikInputArgs)));
        return processLauncher.startProcessesNonBlocking(Collections.singletonList(mosaikLaunchArgs), mosaikRedirectErrorStream);
    }

    private void processResults(List<ProcessReturnInformation> simulatorsResults, ProcessReturnInformation mosaikResult, TestRun testRun, Path testsRunDirectory) {
        testRun.setEndDate(LocalDateTime.now(clock));
        testRun.setTestRunStatus(TestRunStatus.FINISHED);

        boolean simulatorsSuccess = true;
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < simulatorsResults.size(); i++) {
            ProcessReturnInformation simulatorsResult = simulatorsResults.get(i);
            simulatorsSuccess = simulatorsSuccess && simulatorsResult.getExitCode() == 0;

            stringBuilder.append(simulatorsResult.getProcessArgs()).append(NL);
            stringBuilder.append(simulatorsResult.getStdout()).append(NL);
            stringBuilder.append(simulatorsResult.getErrout()).append(NL);
            stringBuilder.append("Exit code: ").append(simulatorsResult.getExitCode()).append(NL);
            stringBuilder.append("----------").append(NL).append(NL);

            try {
                Files.write(testsRunDirectory.resolve("log" + i + ".txt"),
                        stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
            } catch (IOException ex) {
                //not a simulator critical exception
                logger.error("FAILED TO WRITE LOG FILE", ex);
            }
            stringBuilder.setLength(0);
        }
        stringBuilder.setLength(0);


        stringBuilder.append(mosaikResult.getProcessArgs()).append(NL);
        stringBuilder.append(mosaikResult.getStdout()).append(NL);
        stringBuilder.append(mosaikResult.getErrout()).append(NL);
        stringBuilder.append("Exit code: ").append(mosaikResult.getExitCode()).append(NL);
        stringBuilder.append("----------").append(NL).append(NL);

        //write log file
        try {
            Files.write(testsRunDirectory.resolve("log-mosaik.txt"), stringBuilder.toString().getBytes(Charset.forName("UTF-8")));
        } catch (IOException ex) {
            //not a simulator critical exception
            logger.error("FAILED TO WRITE LOG FILE", ex);
        }

        //generate incident:
        if (!simulatorsSuccess || mosaikResult.getExitCode() != 0) {
            Incident incident = new Incident();
            incident.setTestRun(testRun);
            incident.setDescription("Simulators failed: " + simulatorsSuccess + " ; mosaik failed: " + mosaikResult.getExitCode());
            incidentRepository.create(incident);
            testRun.setEvaluation(TestRunEvaluation.OUT_OF_RANGE);
            testRunRepository.update(testRun);
            return;
        }


        //process test evaluator result
        String testResult;
        String measuredMeasures;
        try {
            testResult = testRunResultReader.getTestResult(testsRunDirectory.resolve("testresult.xml"));
            measuredMeasures = testRunResultReader.getMeasuresJson(testsRunDirectory.resolve("testresult.xml"));
        } catch (IOException ex) {
            generateIncident(testRun, ex, "Failed to read results file");
            return;
        }

        if (testResult.equalsIgnoreCase("true")) {
            testRun.setEvaluation(TestRunEvaluation.SUCCESS);
        } else {
            testRun.setEvaluation(TestRunEvaluation.FAIL);
        }

        TypeReference<List<Measure>> typeArrayListMeasures = new TypeReference<List<Measure>>() {
        };

        try {
            List<Measure> deserializedMeasures = mapper.readValue(measuredMeasures, typeArrayListMeasures);
            measureRepository.create(deserializedMeasures);

            testRun.setFinalMeasures(deserializedMeasures);
        } catch (IOException ex) {
            generateIncident(testRun, ex, "Failed to read measure data file");
            return;
        }

        //persist results
        testRunRepository.update(testRun);
    }
}
