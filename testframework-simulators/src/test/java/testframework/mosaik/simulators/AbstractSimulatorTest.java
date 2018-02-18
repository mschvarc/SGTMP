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

import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import testframework.mosaik.util.AsyncDataHolder;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractSimulatorTest {

    private static final String STEP_SIZE = "step_size";
    private static final String EID_PREFIX = "eid_prefix";
    private final Gson gson = new Gson();

    @Test(expected = IllegalStateException.class)
    public void testIllegalStateTransition() {
        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        simulator.create(1, "Model", new HashMap<>());
    }

    @Test
    public void testBasicInit() {
        Map<String, Object> simParams = new HashMap<>();
        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        Map<String, Object> simulationReturn = simulator.init("simId", simParams);
        assertThat(simulationReturn).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void testRepeatedInit() {
        Map<String, Object> simParams = new HashMap<>();
        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        simulator.init("simId", simParams);
        simulator.init("simId", simParams);
    }

    @Test
    public void testBasicCreate() {
        Map<String, Object> simParams = new HashMap<>();
        simParams.put("__eid__", "__0");
        simParams.put("__model__", "Model");

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);

        Simulator simulator = new Simulator(simulationController, "testSim");
        simulator.init("simId", simParams);

        List<Map<String, Object>> models = simulator.create(1, "Model", simParams);

        Mockito.verify(simulationController, times(1)).addModel("Model", simParams);
        assertThat(models.size()).isEqualTo(1);
        assertThat(models.get(0)).containsOnlyKeys("eid", "rel", "type");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBasicCreateAsync() {
        JSONArray array = new JSONArray();
        JSONArray innerArray1 = new JSONArray();
        innerArray1.add("var1OUT");
        innerArray1.add("var1");
        innerArray1.add("syncTarget_v1");
        JSONArray innerArray2 = new JSONArray();
        innerArray2.add("var2OUT");
        innerArray2.add("var2");
        innerArray2.add("syncTarget_v2");
        array.add(innerArray1);
        array.add(innerArray2);

        List<AsyncDataHolder> expected = new ArrayList<>();
        expected.add(new AsyncDataHolder("var1OUT", "var1", "syncTarget_v1"));
        expected.add(new AsyncDataHolder("var2OUT", "var2", "syncTarget_v2"));


        Map<String, Object> simParams = new HashMap<>();
        HashMap<String, Object> inputMap = new HashMap<>();
        inputMap.put("asyncInfo", array);

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");

        simulator.init("simId", simParams);
        simulator.create(1, "Model", inputMap);

        assertThat(simulator.getAsyncConnections()).containsExactlyElementsOf(expected);

    }

    @Test
    public void testStep() throws Exception {
        Map<String, Object> createParams = new HashMap<>();
        Map<String, Object> simParams = new HashMap<>();
        simParams.put(STEP_SIZE, 1);
        Map<String, Object> inputMap = new HashMap<>();

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);

        Simulator simulator = new Simulator(simulationController, "testSim");

        simulator.init("simId", simParams);
        simulator.create(1, "Model", createParams);
        long nextStep = simulator.step(1, inputMap);
        assertThat(nextStep).isEqualTo(2);
        verify(simulationController, times(1)).step(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInputParsing() throws Exception {
        Map<String, Object> createParams = new HashMap<>();
        Map<String, Object> simParams = new HashMap<>();
        simParams.put(STEP_SIZE, 1);
        simParams.put(EID_PREFIX, "eid");


        Map<String, Object> inputMap = new HashMap<String, Object>() {{
            put("eid0", new JSONObject() {{
                put("attributeIN", new JSONObject() {{
                    put("FROM_SIMULATOR_OUTSIDE", 5);
                }});
                put("unrelatedAttribute", new JSONObject() {{
                    put("FROM_SIMULATOR_OUTSIDE", 100);
                }});
            }});
        }};

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);

        Simulator simulator = new Simulator(simulationController, "testSim");

        simulator.init("simId", simParams);
        simulator.create(1, "Model", createParams);

        simulator.step(1, inputMap);

        verify(simulationController, times(1)).setValue(0, "attributeIN", 5);

    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void testInputInvalidEid() throws Exception {
        Map<String, Object> createParams = new HashMap<>();
        Map<String, Object> simParams = new HashMap<>();
        simParams.put(STEP_SIZE, 1);
        simParams.put(EID_PREFIX, "eid");


        Map<String, Object> inputMap = new HashMap<String, Object>() {{
            put("invalidEID", new JSONObject() {{
                put("attributeIN", new JSONObject() {{
                    put("FROM_SIMULATOR_OUTSIDE", 5);
                }});
                put("unrelatedAttribute", new JSONObject() {{
                    put("FROM_SIMULATOR_OUTSIDE", 100);
                }});
            }});
        }};

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        simulator.init("simId", simParams);
        simulator.create(1, "Model", createParams);
        simulator.step(1, inputMap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAsyncCommands() throws Exception {
        final int targetAction = 1234;
        final String eidPrefix = "eid";
        final String actualEid = eidPrefix + "0";
        final String localValueOut = "localValueOUT";
        final String otherSimulatorAsyncInput = "targetValueIN";
        final String syncTargetName = "syncTarget";
        final String destinationSimulator = "Simulator2";

        Map<String, Object> createParams = new HashMap<String, Object>() {{
            put("asyncInfo", new JSONArray() {{
                add(new JSONArray() {{
                    add(localValueOut);
                    add(otherSimulatorAsyncInput);
                    add(syncTargetName);
                }});
            }});
        }};
        Map<String, Object> simParams = new HashMap<>();
        simParams.put(STEP_SIZE, 1);
        simParams.put(EID_PREFIX, "eid");

        Map<String, Object> inputMap = new HashMap<String, Object>() {{
            put(actualEid, new JSONObject() {{
                put("otherIN", new JSONObject() {{
                    put(destinationSimulator, 5);
                }});
                put(syncTargetName, new JSONObject() {{
                    put(destinationSimulator, "value");
                }});
            }});
        }};

        Map<String, Object> expected = new HashMap<String, Object>() {{
            put(actualEid, new HashMap<String, Object>() {{
                put(destinationSimulator, new HashMap<String, Object>() {{
                    put(otherSimulatorAsyncInput, targetAction);
                }});
            }});
        }};

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        when(simulationController.getValue(0, localValueOut)).thenReturn(targetAction);

        Simulator simulator = new Simulator(simulationController, "testSim");

        simulator.init("simId", simParams);
        simulator.create(1, "Model", createParams);

        Map<String, Object> result = simulator.prepareASyncData(inputMap, localValueOut, otherSimulatorAsyncInput, syncTargetName);

        assertThat(result).containsOnlyKeys(actualEid);
        assertThat(result).containsAllEntriesOf(expected);
    }

    @Test
    public void getDataTest() throws Exception {
        Map<String, Object> createParams = new HashMap<>();
        Map<String, Object> simParams = new HashMap<>();
        simParams.put(STEP_SIZE, 1);
        simParams.put(EID_PREFIX, "eid");

        final String value1 = "value1";
        final Long value2 = 1234L;
        final String value1key = "value1key";
        final String value2key = "value2key";
        final String eid = "eid0";


        Map<String, List<String>> outputRequests = new HashMap<String, List<String>>() {{
            put(eid, Arrays.asList(value1key, value2key));
        }};


        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        when(simulationController.getValue(0, value1key)).thenReturn(value1);
        when(simulationController.getValue(0, value2key)).thenReturn(value2);

        Simulator simulator = new Simulator(simulationController, "testSim");
        simulator.init("simId", simParams);
        simulator.create(1, "Model", createParams);

        Map<String, Object> data = simulator.getData(outputRequests);

        final Map<String, Map<String, Object>> expected = new HashMap<String, Map<String, Object>>() {{
            put(eid, new HashMap<String, Object>() {{
                put(value1key, value1);
                put(value2key, value2);
            }});
        }};

        verify(simulationController, times(1)).getValue(0, value1key);
        verify(simulationController, times(1)).getValue(0, value2key);
        verify(simulationController, times(0)).setValue(anyInt(), anyString(), any());
        assertThat(data).containsAllEntriesOf(expected);
    }

    @Test
    public void testParamsToAddEmpty() {
        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        List<String> result = simulator.paramsToAdd(new HashMap<>());
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testParamsToAdd() {

        List<String> expectedInputs = new ArrayList<String>() {{
            add("input1");
            add("input2");
        }};

        List<String> additionalData = new ArrayList<String>() {{
            add("abcd");
            add("defg");
        }};


        JSONArray expected = new JSONArray() {{
            addAll(expectedInputs);
        }};
        String json = expected.toJSONString();

        final Map<String, Object> map = new HashMap<String, Object>() {{
            put("__additionalParams__", json);
            put("otherParams", gson.toJson(additionalData));
        }};

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);

        Simulator simulator = new Simulator(simulationController, "testSim");
        List<String> result = simulator.paramsToAdd(map);
        assertThat(result).containsExactlyElementsOf(expectedInputs);
    }

    @Test
    public void testAttributesEmpty() {
        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);
        Simulator simulator = new Simulator(simulationController, "testSim");
        List<String> result = simulator.attributesToAdd(new HashMap<>());
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAttributesToAdd() {

        List<String> expectedInputs = new ArrayList<String>() {{
            add("input1");
            add("input2");
        }};

        List<String> additionalData = new ArrayList<String>() {{
            add("abcd");
            add("defg");
        }};


        JSONArray expected = new JSONArray() {{
            addAll(expectedInputs);
        }};
        String json = expected.toJSONString();

        final Map<String, Object> map = new HashMap<String, Object>() {{
            put("__additionalInputs__", json);
            put("otherParams", gson.toJson(additionalData));
        }};

        AbstractSimulationController simulationController = mock(AbstractSimulationController.class);

        Simulator simulator = new Simulator(simulationController, "testSim");
        List<String> result = simulator.attributesToAdd(map);
        assertThat(result).containsExactlyElementsOf(expectedInputs);
    }


    private class Simulator extends AbstractSimulator {
        Simulator(AbstractSimulationController abstractSimulationController, String simName) {
            super(abstractSimulationController, simName);
        }
    }

}
