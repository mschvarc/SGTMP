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

package testframework.mosaik.util;

import de.offis.mosaik.api.utils.MetaBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Used for adding SGTMP specific values to simulator metadata
 */
public final class FrameworkMetaBuilder extends MetaBuilder {


    /**
     * Adds list of parameters to metadata
     *
     * @param modelName  model id
     * @param paramNames list of parameter names
     */
    @SuppressWarnings("unchecked")
    public void addParamsToModel(String modelName, List<String> paramNames) {
        JSONObject model = (JSONObject) models.get(modelName);
        JSONArray params = (JSONArray) model.get("params");
        params.addAll(paramNames);
    }

    /**
     * Adds attributes to metadata
     *
     * @param modelName model id
     * @param attrNames list of attribute names
     */
    @SuppressWarnings("unchecked")
    public void addAttrsToModel(String modelName, List<String> attrNames) {
        JSONObject model = (JSONObject) models.get(modelName);
        JSONArray params = (JSONArray) model.get("attrs");
        params.addAll(attrNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getOutput() {
        Map<String, Object> result = super.getOutput();
        //inject framework values
        for (Object modelObject : ((JSONObject) result.get("models")).values()) {
            JSONObject modelsEntry = (JSONObject) modelObject;
            JSONArray params = (JSONArray) modelsEntry.get("params");
            JSONArray attrs = (JSONArray) modelsEntry.get("attrs");

            params.add("__additionalParams__");
            params.add("__model_index_user__");
            attrs.add("__model_index_user__");
        }
        return result;
    }
}
