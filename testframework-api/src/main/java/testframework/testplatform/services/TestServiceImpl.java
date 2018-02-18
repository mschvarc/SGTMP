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
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.filter.TestFilter;
import testframework.testplatform.dal.repository.TestRepository;

import java.util.List;

@Service
public class TestServiceImpl implements TestService {


    private final TestRepository testRepository;

    @Autowired
    public TestServiceImpl(TestRepository testRunRepository) {
        this.testRepository = testRunRepository;
    }

    @Override
    public Test getById(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be < 0");
        }
        return testRepository.getById(id);
    }

    @Override
    public void create(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("test is null");
        }
        testRepository.create(test);
    }

    @Override
    public void update(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("test is null");
        }
        testRepository.update(test);
    }

    @Override
    public void delete(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("test is null");
        }
        testRepository.delete(test);
    }

    @Override
    public List<Test> find(TestFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter is null");
        }
        return testRepository.find(filter);
    }

}
