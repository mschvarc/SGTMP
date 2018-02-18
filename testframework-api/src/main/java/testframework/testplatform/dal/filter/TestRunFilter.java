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

package testframework.testplatform.dal.filter;

import testframework.testplatform.dal.entities.TestRunStatus;

import java.util.EnumSet;
import java.util.Set;

public class TestRunFilter implements EntityFilter {
    private Long id;
    private Long testId;
    private Set<TestRunStatus> testRunStates;

    public Set<TestRunStatus> getTestRunStates() {
        return testRunStates;
    }

    public void setTestRunStates(Set<TestRunStatus> testRunStates) {
        this.testRunStates = testRunStates;
    }

    public void setTestRunStatus(TestRunStatus testRunStatus) {
        this.testRunStates = EnumSet.of(testRunStatus);
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
