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

package testframework.mosaik.simulators;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import testframework.mosaik.models.factory.GlobalTestEvaluatorFactory;
import testframework.mosaik.simulationcontrollers.GlobalTestEvaluatorSimulationController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalTestEvaluatorSimulatorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private GlobalTestEvaluatorSimulator simulator;

    @Before
    public void setup() {
        GlobalTestEvaluatorFactory modelFactory = new GlobalTestEvaluatorFactory();

        GlobalTestEvaluatorSimulationController simulationController =
                new GlobalTestEvaluatorSimulationController(modelFactory);
        simulator = new GlobalTestEvaluatorSimulator(simulationController);
    }


    @Test
    public void testBasicFunctionality() throws Exception {
        Map<String, Object> simParams = new HashMap<>();
        Map<String, Object> createParams = new HashMap<>();

        simParams.put("step_size", 1);

        createParams.put("__StandaloneTestEvaluator__", true);
        createParams.put("__PermanentFailureEntry__", false);
        createParams.put("__TestResultName__", "test name");
        createParams.put("__TestResultPath__", testFolder.newFile().toPath().toAbsolutePath().toString());
        createParams.put("__InputCount__", 0);
        createParams.put("__MeasuresCount__", 0);
        createParams.put("__GlobalMeasureIdMapping__", getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json"));
        createParams.put("__GlobalTargetMeasures__", getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json"));


        simulator.init("GlobalTestEvaluatorSimulator", simParams);
        simulator.create(1, "ModelGlobalTestEvaluator", createParams);

        Map<String, Object> inputs = new HashMap<>();

        simulator.step(1, inputs);
        Map<String, List<String>> outputs = new HashMap<>();
        simulator.getData(outputs);
        simulator.cleanup();
    }

    @Test(expected = IllegalStateException.class)
    public void testMultipleCreation() throws Exception {
        Map<String, Object> simParams = new HashMap<>();
        Map<String, Object> createParams = new HashMap<>();

        simParams.put("step_size", 1);

        createParams.put("__StandaloneTestEvaluator__", true);
        createParams.put("__PermanentFailureEntry__", false);
        createParams.put("__TestResultName__", "test name");
        createParams.put("__TestResultPath__", testFolder.newFile().toPath().toAbsolutePath().toString());
        createParams.put("__InputCount__", 0);
        createParams.put("__MeasuresCount__", 0);
        createParams.put("__GlobalMeasureIdMapping__", getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json"));
        createParams.put("__GlobalTargetMeasures__", getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json"));


        simulator.init("GlobalTestEvaluatorSimulator", simParams);
        simulator.create(2, "ModelGlobalTestEvaluator", createParams);

    }

    private String getResourceContent(String strPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream(strPath), Charset.forName("UTF-8"));
    }

}
