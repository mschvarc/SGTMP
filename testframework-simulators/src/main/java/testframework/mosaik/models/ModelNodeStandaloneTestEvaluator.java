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
 * Standalone test evaluator, is a separate model, Mosaik is aware of its existence
 */
public abstract class ModelNodeStandaloneTestEvaluator extends ModelNodeTestEvaluator {
    public ModelNodeStandaloneTestEvaluator(Map<String, Object> modelParams) {
        super(modelParams);
    }
}
