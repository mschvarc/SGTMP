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

package testframework.testplatform.selection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import testframework.testplatform.dal.entities.SimulatorRequirement;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.wireentities.WireSimulator;
import testframework.testplatform.dal.filter.TestRunFilter;
import testframework.testplatform.dal.repository.TestRunRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GreedyTestRunSelector implements TestRunSelectionStrategy {

    private final TestRunRepository testRunRepository;

    @Autowired
    public GreedyTestRunSelector(TestRunRepository testRunRepository) {
        this.testRunRepository = testRunRepository;
    }

    @Override
    public List<TestRun> selectTestRunsToStart() {
        TestRunFilter startableFilter = new TestRunFilter();
        startableFilter.setTestRunStates(EnumSet.of(TestRunStatus.CREATED, TestRunStatus.WAITING));

        TestRunFilter runningFilter = new TestRunFilter();
        runningFilter.setTestRunStatus(TestRunStatus.STARTED);

        List<TestRun> createdTestRuns = testRunRepository.find(startableFilter);
        List<TestRun> runningTestRuns = testRunRepository.find(runningFilter);

        return selectFirstPossible(createdTestRuns, runningTestRuns);
    }

    private List<TestRun> selectFirstPossible(List<TestRun> createdTestRuns, List<TestRun> runningTestRuns) {
        List<WireSimulator> runningNonVirtualSimulators = new ArrayList<>();
        runningTestRuns.parallelStream()
                .forEach(testRuns -> testRuns.getTest().getSimulatorRequirements()
                        .parallelStream()
                        .forEach(simReq -> simReq.getSimulators()
                                .stream()
                                .filter(req -> !req.isVirtual()).forEach(runningNonVirtualSimulators::add)));

        for (TestRun createdTestRun : createdTestRuns) {
            List<WireSimulator> nonVirtualReq = new LinkedList<>();
            for (SimulatorRequirement simulatorRequirement : createdTestRun.getTest().getSimulatorRequirements()) {
                List<WireSimulator> toAdd = simulatorRequirement.getSimulators()
                        .stream()
                        .filter(sim -> !sim.isVirtual())
                        .collect(Collectors.toList());
                nonVirtualReq.addAll(toAdd);
            }
            if (Collections.disjoint(nonVirtualReq, runningNonVirtualSimulators)) {
                return Collections.singletonList(createdTestRun);
            }
        }
        return Collections.emptyList();

    }
}
