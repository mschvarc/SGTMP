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

import java.util.Map;

public interface ModelFactory {

    /**
     * Creates a new model from FActory with given modelParams
     *
     * @param modelParams params to pass to the model
     * @return new Model
     */
    Model createNewModel(Map<String, Object> modelParams);

}
