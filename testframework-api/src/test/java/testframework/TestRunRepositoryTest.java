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
import testframework.testplatform.dal.filter.TestRunFilter;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class TestRunRepositoryTest {

    @Autowired
    TestRepository testRepository;
    @Autowired
    TestRunRepository testRunRepository;


    @org.junit.Test
    public void testFindAll() {
        Test test = new Test();
        test.setTestDescription("test description");
        test.setTestName("test name");
        test.setUuid(UUID.fromString("0313c600-b72d-11e7-abc4-cec278b6b50a").toString());
        testRepository.create(test);

        TestRun testRunExpected = new TestRun();
        testRunExpected.setTestRunStatus(TestRunStatus.CREATED);
        testRunExpected.setTest(test);
        testRunExpected.setEvaluation(TestRunEvaluation.SUCCESS);
        testRunRepository.create(testRunExpected);

        TestRun testRun2 = new TestRun();
        testRun2.setTestRunStatus(TestRunStatus.CREATED);
        testRun2.setTest(test);
        testRun2.setEvaluation(TestRunEvaluation.SUCCESS);
        testRunRepository.create(testRun2);

        TestRunFilter filter = new TestRunFilter();
        filter.setTestId(test.getId());
        filter.setTestRunStates(EnumSet.of(TestRunStatus.CREATED));
        filter.setId(testRunExpected.getId());

        List<TestRun> result = testRunRepository.find(filter);

        assertThat(result).containsExactly(testRunExpected);

    }

    @org.junit.Test
    public void testFindMultiple() {
        Test test1 = new Test();
        test1.setTestDescription("test description1");
        test1.setTestName("test name1");
        test1.setUuid(UUID.fromString("0313c600-b72d-11e7-abc4-cec278b6b50a").toString());
        testRepository.create(test1);

        Test test2 = new Test();
        test2.setTestDescription("test description2");
        test2.setTestName("test name2");
        test2.setUuid(UUID.fromString("1313c600-b72d-11e7-abc4-cec278b6b51a").toString());
        testRepository.create(test2);

        TestRun testRun1 = new TestRun();
        testRun1.setTestRunStatus(TestRunStatus.FINISHED);
        testRun1.setTest(test2);
        testRun1.setEvaluation(TestRunEvaluation.SUCCESS);
        testRunRepository.create(testRun1);

        TestRun testRun2 = new TestRun();
        testRun2.setTestRunStatus(TestRunStatus.STARTED);
        testRun2.setTest(test2);
        testRunRepository.create(testRun2);

        TestRunFilter filter = new TestRunFilter();
        filter.setTestId(test2.getId());
        filter.setTestRunStates(EnumSet.of(TestRunStatus.FINISHED, TestRunStatus.STARTED));

        List<TestRun> result = testRunRepository.find(filter);

        assertThat(result).containsExactlyInAnyOrder(testRun1, testRun2);

    }
}
