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

package testframework.testplatform.configurationgenerator;

import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MeasureMapOrderForStep {
    private final List<MeasureAttribute> measureAttributeList;
    private final List<ExpectedResult> expectedResultList;
    private final long stepNumber;

    MeasureMapOrderForStep(Map<MeasureAttribute, ExpectedResult> attributeResultMapping, long stepNumber) {
        measureAttributeList = new ArrayList<>();
        expectedResultList = new ArrayList<>();
        this.stepNumber = stepNumber;

        for (Map.Entry<MeasureAttribute, ExpectedResult> measureAttributeExpectedResultEntry : attributeResultMapping.entrySet()) {
            //DO NOT replace this loop with addAll, order of K/V must be guaranteed to be synchronized, cannot de-serialize a json hashmap
            measureAttributeList.add(measureAttributeExpectedResultEntry.getKey());
            expectedResultList.add(measureAttributeExpectedResultEntry.getValue());
        }
    }

    long getStepNumber() {
        return stepNumber;
    }

    List<MeasureAttribute> getMeasureAttributeList() {
        return measureAttributeList;
    }

    List<ExpectedResult> getExpectedResultList() {
        return expectedResultList;
    }
}
