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

package testframework;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestStep;
import testframework.testplatform.dal.entities.measure.Measure;
import testframework.testplatform.dal.entities.measure.MeasureAttribute;
import testframework.testplatform.dal.entities.measure.MeasureType;
import testframework.testplatform.dal.entities.measure.operators.AndOperand;
import testframework.testplatform.dal.entities.measure.operators.ExpectedResult;
import testframework.testplatform.dal.entities.measure.operators.GreaterThanOperand;
import testframework.testplatform.dal.entities.measure.operators.LessThanOperand;
import testframework.testplatform.dal.repository.ExpectedResultRepository;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestStepRepository;

import javax.transaction.Transactional;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class ExpectedResultsTest {

    private final Test test = new Test();

    @Autowired
    TestRepository testRepository;
    @Autowired
    ExpectedResultRepository expectedResultRepository;
    @Autowired
    TestStepRepository testStepRepository;


    @org.junit.Test
    public void testPersistence() {

        test.setTestDescription("test description");
        test.setTestName("test name");
        test.setUuid(UUID.fromString("4f162f92-f7ed-4580-84a0-2fcd739e746b").toString());
        testRepository.create(test);

        MeasureAttribute measureAttribute1 = new MeasureAttribute();
        measureAttribute1.setSimulatorId("Simulator1");
        measureAttribute1.setModelId("Model1");
        measureAttribute1.setName("Simulator1:Model1:measureValue1");
        measureAttribute1.setMeasureInputName("name1");

        Measure ten = new Measure(10);
        ten.setMeasureAttribute(measureAttribute1);
        ten.setMeasureType(MeasureType.EXPECTED);
        Measure twenty = new Measure(20);
        twenty.setMeasureAttribute(measureAttribute1);
        twenty.setMeasureType(MeasureType.EXPECTED);
        Measure fifty = new Measure(50);
        fifty.setMeasureAttribute(measureAttribute1);
        fifty.setMeasureType(MeasureType.EXPECTED);
        Measure hundred = new Measure(100);
        hundred.setMeasureAttribute(measureAttribute1);
        hundred.setMeasureType(MeasureType.EXPECTED);
        Measure thousand = new Measure(1000);
        thousand.setMeasureAttribute(measureAttribute1);
        thousand.setMeasureType(MeasureType.EXPECTED);

        GreaterThanOperand greaterThanTwenty = new GreaterThanOperand(twenty); //x > 20
        LessThanOperand lessThanHundred = new LessThanOperand(hundred); // x < 100
        AndOperand andOperand = new AndOperand();
        andOperand.add(greaterThanTwenty);
        andOperand.add(lessThanHundred);

        TestStep testStep = new TestStep();
        testStep.setStep(50);
        testStep.setTest(test);
        testStep.getExpectedResults().put(measureAttribute1, andOperand);
        test.getTestSteps().add(testStep);
        expectedResultRepository.create(andOperand);
        testStepRepository.create(testStep);


        ExpectedResult retrieved = expectedResultRepository.getById(andOperand.getId());
        assertThat(retrieved.isInRange(fifty));
    }
}
