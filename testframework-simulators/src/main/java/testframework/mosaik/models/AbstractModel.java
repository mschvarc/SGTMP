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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DataModel for Simulation
 * Simulation logic done in step()
 */
public abstract class AbstractModel implements Model {
    private final Map<String, Object> modelParams;
    private final Map<String, Object> values = new HashMap<>();

    public AbstractModel() {
        modelParams = new HashMap<>();
    }

    public AbstractModel(Map<String, Object> modelParams) {
        this.modelParams = modelParams;
    }

    @Override
    public Object getValue(String key) {
        return values.get(key);
    }

    @Override
    public void setValue(String key, Object value) {
        values.put(key, value);
    }

    protected Map<String, Object> getModelParams() {
        return Collections.unmodifiableMap(modelParams);
    }
}
