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

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testframework.testplatform.selection.GreedyTestRunSelector;
import testframework.testplatform.selection.TestRunSelectionStrategy;
import testframework.testplatform.dal.entities.SimulatorRequirement;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.wireentities.WireSimulator;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class TestRunSelectorTest {

    @Autowired
    TestRunRepository testRunRepository;

    @Autowired
    TestRepository testRepository;

    TestRunSelectionStrategy selector;

    @Before
    public void setup() {
        selector = new GreedyTestRunSelector(testRunRepository);

    }

    @org.junit.Test
    public void testSolutionNotFound() {

        WireSimulator sim1 = new WireSimulator("sim1");
        WireSimulator sim2 = new WireSimulator("sim2");

        Test test1 = new Test();
        SimulatorRequirement requirement1 = new SimulatorRequirement();
        requirement1.setPeriodRequired(Period.of(0, 0, 1));
        requirement1.setSimulators(Arrays.asList(sim1, sim2));
        test1.setTestRequirements(Collections.singletonList(requirement1));
        test1.setTestName("test1");
        testRepository.create(test1);

        TestRun testRunStarted = new TestRun();
        testRunStarted.setTestRunStatus(TestRunStatus.STARTED);
        testRunStarted.setTest(test1);
        testRunRepository.create(testRunStarted);

        TestRun testRunCreated = new TestRun();
        testRunCreated.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated.setTest(test1);
        testRunRepository.create(testRunCreated);

        List<TestRun> testRuns = selector.selectTestRunsToStart();
        assertThat(testRuns).isEmpty();
    }

    @org.junit.Test
    public void testSolutionFound() {

        WireSimulator sim1 = new WireSimulator("sim1");
        WireSimulator sim2 = new WireSimulator("sim2");

        Test test1 = new Test();
        SimulatorRequirement requirement1 = new SimulatorRequirement();
        requirement1.setPeriodRequired(Period.of(0, 0, 1));
        requirement1.setSimulators(Arrays.asList(sim1, sim2));
        test1.setTestRequirements(Collections.singletonList(requirement1));
        test1.setTestName("test1");
        testRepository.create(test1);


        TestRun testRunCreated1 = new TestRun();
        testRunCreated1.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated1.setTest(test1);
        testRunRepository.create(testRunCreated1);

        TestRun testRunCreated2 = new TestRun();
        testRunCreated2.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated2.setTest(test1);
        testRunRepository.create(testRunCreated2);

        List<TestRun> testRuns = selector.selectTestRunsToStart();
        Assert.assertEquals(1, testRuns.size()); //only ONE at a time with greedy selection
        Assert.assertThat(testRuns, anyOf(hasItems(testRunCreated1), hasItems(testRunCreated2)));
    }

    @org.junit.Test
    public void testSolutionFoundWithRunning() {

        WireSimulator sim1 = new WireSimulator("sim1");
        WireSimulator sim2 = new WireSimulator("sim2");


        Test test1 = new Test();
        SimulatorRequirement requirement1 = new SimulatorRequirement();
        requirement1.setPeriodRequired(Period.of(0, 0, 1));
        requirement1.setSimulators(Arrays.asList(sim1));
        test1.setTestRequirements(Collections.singletonList(requirement1));
        test1.setTestName("test1");
        testRepository.create(test1);

        TestRun testRunCreated1 = new TestRun();
        testRunCreated1.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated1.setTest(test1);
        testRunRepository.create(testRunCreated1);

        Test test2 = new Test();
        SimulatorRequirement requirement2 = new SimulatorRequirement();
        requirement2.setPeriodRequired(Period.of(0, 0, 1));
        requirement2.setSimulators(Arrays.asList(sim2));
        test2.setTestRequirements(Collections.singletonList(requirement2));
        test2.setTestName("test1");
        testRepository.create(test2);

        TestRun testRunCreated2 = new TestRun();
        testRunCreated2.setTestRunStatus(TestRunStatus.STARTED);
        testRunCreated2.setTest(test2);
        testRunRepository.create(testRunCreated2);

        List<TestRun> testRuns = selector.selectTestRunsToStart();
        Assert.assertEquals(1, testRuns.size()); //only ONE at a time with greedy selection
        Assert.assertThat(testRuns, hasItems(testRunCreated1));
    }


    @org.junit.Test
    public void testNoSolutionFoundWithTwoSimulators() {

        WireSimulator sim1 = new WireSimulator("sim1", false);
        WireSimulator sim2 = new WireSimulator("sim2", false);

        Test testRequiringBothSims = new Test();
        SimulatorRequirement requireSim1and2 = new SimulatorRequirement();
        requireSim1and2.setPeriodRequired(Period.of(0, 0, 1));
        requireSim1and2.setSimulators(Arrays.asList(sim1, sim2));
        testRequiringBothSims.setTestRequirements(Collections.singletonList(requireSim1and2));
        testRequiringBothSims.setTestName("test1");
        testRepository.create(testRequiringBothSims);

        Test testRequiringSim1 = new Test();
        SimulatorRequirement requireSim1 = new SimulatorRequirement();
        requireSim1.setPeriodRequired(Period.of(0, 0, 1));
        requireSim1.setSimulators(Collections.singletonList(sim1));
        testRequiringSim1.setTestRequirements(Collections.singletonList(requireSim1));
        testRequiringSim1.setTestName("test2");
        testRepository.create(testRequiringSim1);


        TestRun testRunStarted1 = new TestRun();
        testRunStarted1.setTestRunStatus(TestRunStatus.STARTED);
        testRunStarted1.setTest(testRequiringBothSims);
        testRunRepository.create(testRunStarted1);


        TestRun testRunCreated1 = new TestRun();
        testRunCreated1.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated1.setTest(testRequiringSim1);
        testRunRepository.create(testRunCreated1);

        TestRun testRunCreated2 = new TestRun();
        testRunCreated2.setTestRunStatus(TestRunStatus.CREATED);
        testRunCreated2.setTest(testRequiringSim1);
        testRunRepository.create(testRunCreated2);

        List<TestRun> testRuns = selector.selectTestRunsToStart();
        Assert.assertEquals(0, testRuns.size());
    }

}
