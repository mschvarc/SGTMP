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

package testframework.testplatform.facade;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import testframework.testplatform.test.TestRunEnqueuer;
import testframework.testplatform.dal.entities.Test;
import testframework.testplatform.dal.filter.TestFilter;
import testframework.testplatform.mapper.Automapper;
import testframework.testplatform.facade.dto.TestDto;
import testframework.testplatform.services.TestRunService;
import testframework.testplatform.services.TestService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class TestFacadeImpl implements TestFacade {

    private static final Logger logger = Logger.getLogger(TestFacadeImpl.class);
    private final TestService testService;
    private final Automapper mapper;
    private final TestRunEnqueuer testLauncher;

    @Autowired
    public TestFacadeImpl(TestRunService testRunService, Automapper mapper, TestRunEnqueuer testLauncher, TestService testService) {
        this.mapper = mapper;
        this.testLauncher = testLauncher;
        this.testService = testService;
    }

    @Override
    public TestDto createSkeletonTest() {
        TestDto test = new TestDto();
        test.initializeUUID();
        return test;
    }


    @Override
    public List<TestDto> getAllTests() {
        List<Test> tests = testService.find(new TestFilter());
        return mapper.mapTo(tests, TestDto.class);
    }


    @Override
    @Async
    public void runTest(String id) throws IOException {
        logger.info("ENQUEUED TEST " + id + " at: " + (new Date().toString()));
        long testId = Long.parseLong(id);
        testLauncher.enqueueTest(testId);
        logger.info("ENDED ENQUE TEST " + id + " at: " + (new Date().toString()));
    }

    @Override
    public List<TestDto> findAll() {
        TestFilter filter = new TestFilter();
        List<Test> tests = testService.find(filter);
        return mapper.mapTo(tests, TestDto.class);
    }

    @Override
    public TestDto findById(long id) {
        Test test = testService.getById(id);
        return mapper.mapTo(test, TestDto.class);
    }

    @Override
    @Transactional
    public TestDto create(TestDto testDto) {
        Test test = mapper.mapTo(testDto, Test.class);
        testService.create(test);
        return mapper.mapTo(test, TestDto.class);
    }

    @Override
    @Transactional
    public TestDto update(TestDto testDto) {
        Test test = mapper.mapTo(testDto, Test.class);
        testService.update(test);
        return mapper.mapTo(test, TestDto.class);
    }

    @Override
    @Transactional
    public TestDto delete(TestDto testDto) {
        Test test = mapper.mapTo(testDto, Test.class);
        testService.delete(test);
        return mapper.mapTo(test, TestDto.class);
    }

}
