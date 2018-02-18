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

package testframework.testplatform.web.util.storage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import testframework.testplatform.facade.TestFacade;
import testframework.testplatform.facade.dto.TestDto;
import testframework.testplatform.web.models.TestUpload;

@Service
public class TestUploader {

    private static final Logger logger = Logger.getLogger(TestUploader.class);
    private final TestFacade testFacade;

    @Autowired
    public TestUploader(TestFacade testFacade) {
        this.testFacade = testFacade;
    }

    public TestDto createSkeletonTest() {
        TestDto test = new TestDto();
        test.initializeUUID();
        return test;
    }

    public void createTest(TestUpload model, TestDto test) {
        logger.info("Creating new test from uploaded data");
        test.setTestName(model.getTestName());
        test.setOneStepPermanentFailure(model.isOneStepPermanentFailure());
        test.setTestName(model.getTestName());
        test.setTestDescription(model.getTestDescription());
        testFacade.create(test);
    }

}
