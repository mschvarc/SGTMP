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

package testframework.mosaik.example.models.factory;

import testframework.mosaik.example.models.Model1;
import testframework.mosaik.models.Model;
import testframework.mosaik.models.factory.ModelFactory;

import java.util.Map;

public class Model1Factory implements ModelFactory {

    @Override
    public Model createNewModel(Map<String, Object> modelParams) {
        return new Model1(modelParams);
    }
}
