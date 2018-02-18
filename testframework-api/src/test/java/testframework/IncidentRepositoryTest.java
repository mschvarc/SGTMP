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

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import testframework.testplatform.dal.entities.Incident;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.entities.TestRunEvaluation;
import testframework.testplatform.dal.entities.TestRunStatus;
import testframework.testplatform.dal.repository.IncidentRepository;
import testframework.testplatform.dal.repository.TestRepository;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestJPAConfig.class})
@Transactional
public class IncidentRepositoryTest {

    @Autowired
    TestRepository testRepository;
    @Autowired
    TestRunRepository testRunRepository;
    @Autowired
    IncidentRepository incidentRepository;

    private Test test1;
    private TestRun testRun1;
    private TestRun testRun2;

    @Before
    public void before() {
        test1 = new Test();
        test1.setTestDescription("test description1");
        test1.setTestName("test name1");
        test1.setUuid(UUID.fromString("0313c600-b72d-11e7-abc4-cec278b6b50a").toString());
        testRepository.create(test1);


        testRun1 = new TestRun();
        testRun1.setTestRunStatus(TestRunStatus.FINISHED);
        testRun1.setTest(test1);
        testRun1.setEvaluation(TestRunEvaluation.SUCCESS);
        testRunRepository.create(testRun1);

        testRun2 = new TestRun();
        testRun2.setTestRunStatus(TestRunStatus.FINISHED);
        testRun2.setTest(test1);
        testRun2.setEvaluation(TestRunEvaluation.FAIL);
        testRunRepository.create(testRun2);
    }

    @org.junit.Test
    public void testFindAll() {

        Incident incident1 = new Incident();
        incident1.setTestRun(testRun1);
        incident1.setDescription("desc1");
        incidentRepository.create(incident1);

        Incident incident2 = new Incident();
        incident2.setTestRun(testRun2);
        incident2.setDescription("desc1");
        incidentRepository.create(incident2);

        List<Incident> allIncidentsForTestRun = incidentRepository.getAllIncidentsForTestRun(testRun1);
        assertThat(allIncidentsForTestRun).containsExactly(incident1);

    }

    @org.junit.Test
    public void testFindMultiple() {

        Incident incident1 = new Incident();
        incident1.setTestRun(testRun1);
        incident1.setDescription("desc1");
        incidentRepository.create(incident1);

        Incident incident2 = new Incident();
        incident2.setTestRun(testRun1);
        incident2.setDescription("desc1");
        incidentRepository.create(incident2);

        List<Incident> allIncidentsForTestRun = incidentRepository.getAllIncidentsForTestRun(testRun1);
        assertThat(allIncidentsForTestRun).containsExactlyInAnyOrder(incident1, incident2);

    }
}
