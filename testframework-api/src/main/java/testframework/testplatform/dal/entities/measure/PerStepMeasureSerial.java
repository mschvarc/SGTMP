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

package testframework.testplatform.dal.entities.measure;

import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used for JSON (de)serialization and transport only
 */
public class PerStepMeasureSerial {
    private long step;
    private List<MeasureAttribute> measureAttributes;
    private List<ExpectedResult> expectedResults;

    public PerStepMeasureSerial() {
    }

    public PerStepMeasureSerial(long step, List<MeasureAttribute> measureAttributes, List<ExpectedResult> expectedResults) {
        this.step = step;
        this.measureAttributes = measureAttributes;
        this.expectedResults = expectedResults;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public List<MeasureAttribute> getMeasureAttributes() {
        return measureAttributes;
    }

    public void setMeasureAttributes(List<MeasureAttribute> measureAttributes) {
        this.measureAttributes = measureAttributes;
    }

    public List<ExpectedResult> getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(List<ExpectedResult> expectedResults) {
        this.expectedResults = expectedResults;
    }

    public synchronized PerStepMeasure toPerStepMeasure() {
        assert measureAttributes.size() == expectedResults.size();
        Map<MeasureAttribute, ExpectedResult> measureAttributeExpectedResultMap = new HashMap<>();

        for (int i = 0; i < measureAttributes.size(); i++) {
            MeasureAttribute measureAttribute = measureAttributes.get(i);
            ExpectedResult expectedResult = expectedResults.get(i);
            measureAttributeExpectedResultMap.put(measureAttribute, expectedResult);
        }

        PerStepMeasure perStepMeasure = new PerStepMeasure();
        perStepMeasure.setStep(this.step);
        perStepMeasure.setMeasureAttributes(measureAttributeExpectedResultMap);
        return perStepMeasure;

    }
}
