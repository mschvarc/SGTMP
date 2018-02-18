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

package testframework.testplatform.dal.entities;


import testframework.testplatform.dal.entities.measure.Measure;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TestRun extends BaseEntity {

    @ManyToOne(cascade = CascadeType.ALL)
    private Test test;

    //@Temporal(TemporalType.TIMESTAMP) //tracks milliseconds
    private LocalDateTime startDate;
    //@Temporal(TemporalType.TIMESTAMP) //tracks milliseconds
    private LocalDateTime endDate;

    @Enumerated
    private TestRunEvaluation evaluation;

    @Enumerated
    private TestRunStatus testRunStatus;


    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Map<TestTemplateParameter, TestTemplateValue> mappedValues = new HashMap<>();

    @ManyToMany
    private List<Measure> finalMeasures;

    public List<Measure> getFinalMeasures() {
        return finalMeasures;
    }

    public void setFinalMeasures(List<Measure> finalMeasures) {
        this.finalMeasures = finalMeasures;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
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

    public Map<TestTemplateParameter, TestTemplateValue> getMappedValues() {
        return mappedValues;
    }

    public void setMappedValues(List<TestTemplateValue> mappedValues) {
        this.mappedValues.clear();
        for (TestTemplateValue mappedValue : mappedValues) {
            this.mappedValues.put(mappedValue.getTemplateParameter(), mappedValue);
        }
    }

    public void setMappedValues(Map<TestTemplateParameter, TestTemplateValue> mappedValues) {
        this.mappedValues = mappedValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestRun)) return false;
        if (!super.equals(o)) return false;

        TestRun testRun = (TestRun) o;

        if (getTest() != null ? !getTest().equals(testRun.getTest()) : testRun.getTest() != null) return false;
        if (getStartDate() != null ? !getStartDate().equals(testRun.getStartDate()) : testRun.getStartDate() != null)
            return false;
        if (getEndDate() != null ? !getEndDate().equals(testRun.getEndDate()) : testRun.getEndDate() != null)
            return false;
        if (getEvaluation() != testRun.getEvaluation()) return false;
        return getTestRunStatus() == testRun.getTestRunStatus();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTest() != null ? getTest().hashCode() : 0);
        result = 31 * result + (getStartDate() != null ? getStartDate().hashCode() : 0);
        result = 31 * result + (getEndDate() != null ? getEndDate().hashCode() : 0);
        result = 31 * result + (getEvaluation() != null ? getEvaluation().hashCode() : 0);
        result = 31 * result + (getTestRunStatus() != null ? getTestRunStatus().hashCode() : 0);
        return result;
    }
}
