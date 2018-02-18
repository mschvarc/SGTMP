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

import org.springframework.scheduling.annotation.Async;
import testframework.testplatform.facade.dto.TestRunDto;

import java.util.List;

public interface TestRunFacade {
    /**
     * Gets list of all test runs for a specific test
     *
     * @param testId test id
     * @return TestRuns
     */
    List<TestRunDto> getAllTestRunsForTest(long testId);

    /**
     * Gets a single TestRun by its ID
     *
     * @param testRunID id
     * @return TestRun
     */
    TestRunDto getTestRun(long testRunID);

    /**
     * Starts a given test run and gets the results
     * Method is blocking internally
     *
     * @param testRunId test run id
     */
    @Async
    void startTestRun(long testRunId);

}
