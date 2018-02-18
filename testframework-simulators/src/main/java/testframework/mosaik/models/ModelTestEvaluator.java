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

import java.util.HashMap;
import java.util.Map;

/**
 * Model for an evaluator for another model
 */
public interface ModelTestEvaluator extends Model {

    String TEST_RESULT_VAR_NAME = "__TEST_PASS__";

    /**
     * Determines the current model's test pass state
     *
     * @return true if all criteria are met
     */
    boolean isPassed();

    /**
     * Gets all messages from simulation run for collection
     *
     * @return messages
     */
    String getMessages();

    /**
     * Allows returning additional data for the last simulation step
     *
     * @return return data for processing after simulation ends
     */
    default Map<String, String> lastSimulationStep() {
        return new HashMap<>();
    }


}
