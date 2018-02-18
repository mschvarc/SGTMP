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

package testframework.mosaik.dto.measure.operators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import testframework.mosaik.dto.measure.Measure;

import java.util.ArrayList;
import java.util.List;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AndOperand.class, name = "AndOperand"),
})
public class AndOperand extends CompositeOperand {


    private List<ExpectedResult> expectedResults = new ArrayList<>();

    public List<ExpectedResult> getExpectedResults() {
        if (expectedResults == null) {
            return new ArrayList<>();
        }
        return expectedResults;
    }

    public void setExpectedResults(List<ExpectedResult> expectedResults) {
        this.expectedResults = expectedResults;
    }

    @Override
    public void add(ExpectedResult item) {
        expectedResults.add(item);
    }

    @Override
    public void remove(ExpectedResult item) {
        expectedResults.remove(item);
    }

    @Override
    public List<ExpectedResult> getChildren() {
        if (expectedResults == null) {
            return new ArrayList<>();
        }
        return expectedResults;
    }


    @Override
    public boolean isInRange(Measure measure) {
        for (ExpectedResult expectedResult : expectedResults) {
            if (!expectedResult.isInRange(measure)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "AndOperand{" +
                "expectedResults=" + expectedResults +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AndOperand)) return false;
        if (!super.equals(o)) return false;

        AndOperand that = (AndOperand) o;

        return getExpectedResults() != null ? getExpectedResults().equals(that.getExpectedResults()) : that.getExpectedResults() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getExpectedResults() != null ? getExpectedResults().hashCode() : 0);
        return result;
    }
}
