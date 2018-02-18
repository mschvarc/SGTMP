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
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunEvaluation;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.entities.TestTemplateParameter;
import testframework.testplatform.dal.entities.TestTemplateValue;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;
import testframework.testplatform.dal.repository.TestTemplateParameterRepository;
import testframework.testplatform.dal.repository.TestTemplateValuesRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class TestTemplateParameterRepositoryTest {

    private final TestTemplateParameter testTemplateParameter = new TestTemplateParameter();
    private final Test test = new Test();
    private final TestRun testRun = new TestRun();
    @Autowired
    TestRepository testRepository;
    @Autowired
    TestRunRepository testRunRepository;
    @Autowired
    TestTemplateValuesRepository testTemplateValuesRepository;
    @Autowired
    TestTemplateParameterRepository testTemplateParametersRepository;


    private static LocalDateTime date(long stamp) {
        Date date = new Date();
        date.setTime(stamp);
        return LocalDateTime.ofEpochSecond(stamp, 0, ZoneOffset.UTC);
    }

    @org.junit.Before
    public void setup() {
        testTemplateParameter.setKey("variableA");
        testTemplateParameter.setMaximumValue(100L);
        testTemplateParameter.setMinimumValue(0L);
        testTemplateParameter.setReplaceMode(TestTemplateParameter.ReplaceMode.TEST_BOUNDARY_AND_MID);
        testTemplateParametersRepository.create(testTemplateParameter);

        test.setTestDescription("desc");
        test.setTestName("name");
        testRepository.create(test);

        testRun.setStartDate(date(1000));
        testRun.setEndDate(date(5000));
        testRun.setEvaluation(TestRunEvaluation.SUCCESS);
        testRun.setTest(test);
        testRun.setTestRunStatus(TestRunStatus.FINISHED);

        Map<TestTemplateParameter, TestTemplateValue> values = new HashMap<>();
        TestTemplateValue testTemplateValueKey = new TestTemplateValue(testTemplateParameter, this.testTemplateParameter.getKey(), 50L);
        values.put(this.testTemplateParameter, testTemplateValueKey);
        testRun.setMappedValues(values);
        testRunRepository.create(testRun);

        List<TestTemplateParameter> paramsList = new ArrayList<>();
        paramsList.add(testTemplateParameter);
        test.setTemplateValues(paramsList);
        testRepository.update(test);
    }

    @org.junit.Test
    public void testIntegrity() {
        Test retrievedTest = testRepository.getById(test.getId());
        TestRun retrievedTestRun = testRunRepository.getById(testRun.getId());
        assertEquals(TestRunEvaluation.SUCCESS, retrievedTestRun.getEvaluation());
        assertEquals(TestRunStatus.FINISHED, retrievedTestRun.getTestRunStatus());
        assertEquals("name", retrievedTest.getTestName());
    }

}
