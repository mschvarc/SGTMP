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

package testframework.testplatform.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import testframework.testplatform.test.TestRunner;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.filter.TestRunFilter;
import testframework.testplatform.mapper.Automapper;
import testframework.testplatform.facade.dto.TestRunDto;
import testframework.testplatform.services.TestRunService;

import java.util.List;

@Service
public class TestRunFacadeImpl implements TestRunFacade {

    private final TestRunService testRunService;
    private final TestRunner testRunner;
    private final Automapper mapper;

    @Autowired
    public TestRunFacadeImpl(TestRunService testRunService, TestRunner testRunner, Automapper mapper) {
        this.testRunService = testRunService;
        this.testRunner = testRunner;
        this.mapper = mapper;
    }

    @Override
    public List<TestRunDto> getAllTestRunsForTest(long testId) {
        TestRunFilter filter = new TestRunFilter();
        filter.setTestId(testId);
        List<TestRun> testRuns = testRunService.find(filter);
        return mapper.mapTo(testRuns, TestRunDto.class);
    }

    @Override
    public TestRunDto getTestRun(long testRunID) {
        TestRun testRun = testRunService.getById(testRunID);
        return mapper.mapTo(testRun, TestRunDto.class);
    }


    @Override
    @Async
    public void startTestRun(long testRunId) {
        testRunner.startTestRunNonBlocking(testRunId);
        testRunner.getTestRunResultBlocking(testRunId);
    }

}
