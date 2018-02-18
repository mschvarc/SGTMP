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

package testframework.mosaik.models;

import java.util.Map;

/**
 * Super class for all test evaluators, determines the test results of each simulation model
 */
public abstract class ModelNodeTestEvaluator extends AbstractModel implements ModelTestEvaluator {

    private boolean testPassed = false;
    private StringBuilder messages = new StringBuilder();

    public ModelNodeTestEvaluator(Map<String, Object> modelParams) {
        super(modelParams);
    }

    @Override
    public boolean isPassed() {
        return testPassed;
    }

    public void addMesage(String message) {
        messages.append(message);
    }

    public boolean isTestPassed() {
        return testPassed;
    }

    protected void setTestPassed(boolean testPassed) {
        this.testPassed = testPassed;
    }

    @Override
    public String getMessages() {
        return messages.toString();
    }
}
