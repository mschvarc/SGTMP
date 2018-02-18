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

package testframework.mosaik.models.factory;

import testframework.mosaik.models.Model;
import testframework.mosaik.models.ModelGlobalTestEvaluator;
import testframework.mosaik.models.ModelTestEvaluator;
import testframework.mosaik.models.factory.TestEvaluatorFactory;

import java.util.Map;

public class GlobalTestEvaluatorFactory implements TestEvaluatorFactory {
    @Override
    public ModelTestEvaluator createNewStandaloneTestEvaluator(Map<String, Object> modelParams) {
        if (modelParams == null) {
            throw new IllegalArgumentException("modelParams is null");
        }
        return new ModelGlobalTestEvaluator(modelParams);
    }

    @Override
    public ModelTestEvaluator createNewAttachedTestEvaluator(Model parentModel, Map<String, Object> modelParams) {
        if (modelParams == null) {
            throw new IllegalArgumentException("modelParams is null");
        }
        if (parentModel == null) {
            throw new IllegalArgumentException("parentModel is null");
        }
        throw new IllegalStateException("cannot have parent model");
    }
}
