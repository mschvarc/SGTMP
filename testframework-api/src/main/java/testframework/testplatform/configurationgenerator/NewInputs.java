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

package testframework.testplatform.configurationgenerator;

import testframework.testplatform.dal.entities.wireentities.WireModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NewInputs {
    private Map<String, HashMap<String, HashMap<Integer, List<String>>>> map = new HashMap<>();

    NewInputs() {
    }

    void addInputToMap(WireModel model, String input) {
        addInputToMap(model.getSimulator().getSimulatorId(), model.getModelId(), model.getModelIndex(), input);
    }

    void addInputToMap(String simID, String modelId, int index, String input) {
        if (!map.containsKey(simID)) {
            map.put(simID, new HashMap<>());
        }
        if (!map.get(simID).containsKey(modelId)) {
            map.get(simID).put(modelId, new HashMap<>());
        }
        if (!map.get(simID).get(modelId).containsKey(index)) {
            map.get(simID).get(modelId).put(index, new ArrayList<>());
        }
        map.get(simID).get(modelId).get(index).add(input);
    }

    boolean containsNestedKey(String simID) {
        return map.containsKey(simID);
    }


    List<String> getFlatInputsForSimulator(String simID) {
        List<String> results = new ArrayList<>();
        HashMap<String, HashMap<Integer, List<String>>> outerMap = map.get(simID);
        for (HashMap<Integer, List<String>> innerMap : outerMap.values()) {
            for (List<String> strings : innerMap.values()) {
                results.addAll(strings);
            }
        }
        return results;
    }
}
