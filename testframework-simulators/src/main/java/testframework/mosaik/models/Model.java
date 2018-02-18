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

/**
 * Interface Model for a simulator
 * Simulation logic done in step()
 */
public interface Model {

    /**
     * Gets value for specified key
     *
     * @param key key name
     * @return value or null if not found
     */
    Object getValue(String key);

    /**
     * Sets a value for a specified key
     *
     * @param key   key name
     * @param value value to store
     */
    void setValue(String key, Object value);

    /**
     * Called each simulation step
     *
     * @param time current simulation time
     */
    void step(long time);

    /**
     * MUST be called at the end of step()
     * MUST be called at the end of a constructor
     * Inheriting classes MUST call super.synchronizeFieldsToMap() first in own synchronizeFieldsToMap
     */
    void synchronizeFieldsToMap();

    /**
     * MUST be called at the start of step()
     * Inheriting classes MUST call super.synchronizeMapToFields() first in own synchronizeMapToFields
     */
    void synchronizeMapToFields();


}
