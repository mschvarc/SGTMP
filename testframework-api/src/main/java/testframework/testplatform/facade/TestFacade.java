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

import org.springframework.scheduling.annotation.Async;
import testframework.testplatform.facade.dto.TestDto;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

public interface TestFacade {
    TestDto createSkeletonTest();

    List<TestDto> getAllTests();

    @Async
    void runTest(String id) throws IOException;

    TestDto findById(long id);

    List<TestDto> findAll();

    @Transactional
    TestDto create(TestDto testDto);

    @Transactional
    TestDto update(TestDto testDto);

    @Transactional
    TestDto delete(TestDto testDto);
}
