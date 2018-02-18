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

/**
 * Translates user generated XML configuration files to ones that can be accepted by the Mosaik Framework
 * Generates wire_connectors.xml, models.xml, simulators.xml and copies simulation_config.xml without any changes
 */
public interface RuntimeConfigurationGenerator {

    /**
     * Loads user configuration files, parses them and saves them
     *
     * @param filePaths source and destination paths
     */
    void createTestDataModels(String[] filePaths);
}
