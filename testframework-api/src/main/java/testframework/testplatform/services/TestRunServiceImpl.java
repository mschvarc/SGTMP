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

package testframework.testplatform.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testframework.testplatform.dal.entities.TestRun;
import testframework.testplatform.dal.filter.TestRunFilter;
import testframework.testplatform.dal.repository.TestRunRepository;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TestRunServiceImpl implements TestRunService {

    private final TestRunRepository testRunRepository;

    @Autowired
    public TestRunServiceImpl(TestRunRepository testRunRepository) {
        this.testRunRepository = testRunRepository;
    }

    @Override
    public void create(TestRun testRun) {
        if (testRun == null) {
            throw new IllegalArgumentException("testRun is null");
        }
        testRunRepository.create(testRun);
    }

    @Override
    public void update(TestRun testRun) {
        if (testRun == null) {
            throw new IllegalArgumentException("testRun is null");
        }
        testRunRepository.update(testRun);
    }

    @Override
    public void delete(TestRun testRun) {
        if (testRun == null) {
            throw new IllegalArgumentException("testRun is null");
        }
        testRunRepository.delete(testRun);
    }

    @Override
    public TestRun getById(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }
        return testRunRepository.getById(id);
    }

    @Override
    public List<TestRun> find(TestRunFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter is null");
        }
        return testRunRepository.find(filter);
    }
}
