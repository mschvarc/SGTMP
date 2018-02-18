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

import org.springframework.stereotype.Component;
import testframework.testplatform.dal.entities.TestRun;

import java.util.List;

/**
 * Defines strategy for selecting a TestRun from pending TestRuns in the queue
 */
@Component
public interface TestRunSelectionStrategy {

    /**
     * Returns zero or more TestRuns which can be immediately started
     * If more than one TestRun is returned, all of them must be capable of being launched simultaneously
     *
     * @return TestRuns which can be immediately started
     */
    List<TestRun> selectTestRunsToStart();
}
