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

package testframework.mosaik.simulationcontrollers;

import testframework.mosaik.models.factory.TestEvaluatorFactory;

import java.util.Collections;
import java.util.Map;

public class GlobalTestEvaluatorSimulationController extends AbstractSimulationController {

    public GlobalTestEvaluatorSimulationController(TestEvaluatorFactory testEvaluatorFactory) {
        super(testEvaluatorFactory);
    }

    /**
     * Call for last simulation step and do appropriate logic
     *
     * @return final results from GlobalTestEvaluator
     */
    public Map<String, String> lastSimulationStep() {
        if (getTestEvaluators().isEmpty()) {
            return Collections.emptyMap();
        }
        assert getTestEvaluators().size() == 1;
        return getTestEvaluators().get(0).lastSimulationStep();
    }

}
