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

package testframework.testplatform.web.models;

import testframework.testplatform.facade.dto.TestRunDto;

import java.util.ArrayList;
import java.util.List;

public class TestRunsModel {
    private List<TestRunDto> testRuns = new ArrayList<>();

    public List<TestRunDto> getTestRuns() {
        return testRuns;
    }

    public void setTestRuns(List<TestRunDto> testRuns) {
        this.testRuns = testRuns;
    }
}
