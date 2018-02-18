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

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;


public class TestUpload {

    @NotNull
    private MultipartFile models;

    @NotNull
    private MultipartFile simulationConfig;

    @NotNull
    private MultipartFile simulators;

    @NotNull
    private MultipartFile wires;

    private MultipartFile testConfig;

    private String testDescription;

    @NotNull
    @Length(min = 1, max = 255)
    private String testName;

    private boolean oneStepPermanentFailure;

    public boolean isValid() {
        return !(simulationConfig.isEmpty() || simulators.isEmpty() || models.isEmpty() || wires.isEmpty() || testConfig.isEmpty());
    }


    public MultipartFile getTestConfig() {
        return testConfig;
    }

    public void setTestConfig(MultipartFile testConfig) {
        this.testConfig = testConfig;
    }

    public boolean isOneStepPermanentFailure() {
        return oneStepPermanentFailure;
    }

    public void setOneStepPermanentFailure(boolean oneStepPermanentFailure) {
        this.oneStepPermanentFailure = oneStepPermanentFailure;
    }

    public MultipartFile getModels() {
        return models;
    }

    public void setModels(MultipartFile models) {
        this.models = models;
    }

    public MultipartFile getSimulationConfig() {
        return simulationConfig;
    }

    public void setSimulationConfig(MultipartFile simulationConfig) {
        this.simulationConfig = simulationConfig;
    }

    public MultipartFile getSimulators() {
        return simulators;
    }

    public void setSimulators(MultipartFile simulators) {
        this.simulators = simulators;
    }

    public MultipartFile getWires() {
        return wires;
    }

    public void setWires(MultipartFile wires) {
        this.wires = wires;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }
}
