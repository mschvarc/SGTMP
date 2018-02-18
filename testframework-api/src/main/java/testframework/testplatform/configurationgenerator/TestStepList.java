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

import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestStep;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.PerStepMeasureSerial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TestStepList {
    private final List<PerStepMeasureSerial> perStepMeasures;
    private final Set<MeasureAttribute> allMeasureAttributes = new HashSet<>();

    TestStepList(Test test) {
        List<TestStep> testSteps = test.getTestSteps();
        testSteps.sort(Comparator.comparingLong(TestStep::getStep));

        perStepMeasures = new ArrayList<>(testSteps.size());

        for (TestStep testStep : testSteps) {
            allMeasureAttributes.addAll(testStep.getExpectedResults().keySet());

            MeasureMapOrderForStep measureMapOrderForStep = new MeasureMapOrderForStep(
                    testStep.getExpectedResults(), testStep.getStep());

            PerStepMeasureSerial perStepMeasure = new PerStepMeasureSerial(
                    testStep.getStep(),
                    measureMapOrderForStep.getMeasureAttributeList(),
                    measureMapOrderForStep.getExpectedResultList()
            );
            perStepMeasures.add(perStepMeasure);
        }
    }

    List<MeasureAttribute> getAllMeasureAttributes() {
        return new ArrayList<>(allMeasureAttributes);
    }

    List<PerStepMeasureSerial> getPerStepMeasures() {
        return perStepMeasures;
    }

}
