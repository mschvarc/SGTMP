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


import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.HashMap;
import java.util.Map;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TestStep extends BaseEntity {

    private long step;

    @ManyToOne
    private Test test;

    @ManyToMany(cascade = CascadeType.ALL)
    private Map<MeasureAttribute, ExpectedResult> expectedResults = new HashMap<>();

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Map<MeasureAttribute, ExpectedResult> getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(Map<MeasureAttribute, ExpectedResult> expectedResults) {
        this.expectedResults = expectedResults;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestStep)) return false;
        if (!super.equals(o)) return false;

        TestStep testStep = (TestStep) o;

        if (getStep() != testStep.getStep()) return false;
        if (getTest() != null ? !getTest().equals(testStep.getTest()) : testStep.getTest() != null) return false;
        return getExpectedResults() != null ? getExpectedResults().equals(testStep.getExpectedResults()) : testStep.getExpectedResults() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (getStep() ^ (getStep() >>> 32));
        result = 31 * result + (getTest() != null ? getTest().hashCode() : 0);
        result = 31 * result + (getExpectedResults() != null ? getExpectedResults().hashCode() : 0);
        return result;
    }
}
