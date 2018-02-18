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

import testframework.mosaik.example.models.ModelArduino;
import testframework.mosaik.models.factory.ModelFactory;

import java.util.Map;

import static testframework.mosaik.util.Helpers.getInt;

public class ArduinoModelFactory implements ModelFactory {

    public ModelArduino createNewModel(int ledState, String port) {
        return new ModelArduino(ledState, port);
    }

    @Override
    public ModelArduino createNewModel(Map<String, Object> modelParams) {
        return new ModelArduino(getInt(modelParams.get("initLedState")), (String) modelParams.get("targetPort"));
    }
}
