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

package testframework.testplatform.facade.dto;

import testframework.testplatform.dal.entities.TestRunEvaluation;
import testframework.testplatform.dal.entities.TestRunStatus;

import java.time.LocalDateTime;

public class TestRunDto {

    private long id;
    private TestDto test;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TestRunEvaluation evaluation;
    private TestRunStatus testRunStatus;

    public TestDto getTest() {
        return test;
    }

    public void setTest(TestDto test) {
        this.test = test;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public TestRunEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(TestRunEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public TestRunStatus getTestRunStatus() {
        return testRunStatus;
    }

    public void setTestRunStatus(TestRunStatus testRunStatus) {
        this.testRunStatus = testRunStatus;
    }
}
