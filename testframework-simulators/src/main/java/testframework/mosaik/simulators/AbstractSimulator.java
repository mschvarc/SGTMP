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
import de.offis.mosaik.api.Simulator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import testframework.mosaik.util.AsyncDataHolder;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;
import testframework.mosaik.util.FrameworkMetaBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testframework.mosaik.util.Helpers.getInt;

public abstract class AbstractSimulator extends Simulator {


    public static final String MODEL_INDEX_KEY = "__model_index_user__";
    private static final String ENTITY_ID_KEY = "__eid__";
    private static final String ENTITY_MODEL_KEY = "__model__";
    private static final Gson GSON = new Gson();
    private final AbstractSimulationController simulationController;
    private final Map<String, Integer> entities;
    private final List<AsyncDataHolder> asyncConnections = new ArrayList<>();
    private int stepSize = -1; // -1 step size forces runtime error, needs to be set explicitly
    private String eidPrefix = "__";
    private String simulationId;
    private int idCounter = 0;
    private State currentState = State.NEW;

    public AbstractSimulator(AbstractSimulationController abstractSimulationController, String simName) {
        super(simName);
        this.simulationController = abstractSimulationController;
        this.entities = new HashMap<>(); //Maps entity-ID to indices in Simulator
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> init(String sid, Map<String, Object> simParams) {
        if (currentState != State.NEW && currentState != State.FINISHED) {
            throw new IllegalStateException("init called more than once during simulator lifetime");
        }
        currentState = State.INITIALIZED;
        asyncConnections.clear();
        entities.clear();
        simulationController.reset();


        simulationId = sid;
        if (simParams.containsKey("eid_prefix")) {
            this.eidPrefix = simParams.get("eid_prefix").toString();
        }
        if (simParams.containsKey("step_size")) {
            this.stepSize = getInt(simParams.get("step_size"));
        }
        return null;
    }

    private void addAsyncInfo(Map<String, Object> modelParams) {
        JSONArray asyncInfo = (JSONArray) modelParams.get("asyncInfo");
        for (int i = 0; i < asyncInfo.size(); i++) {
            AsyncDataHolder newData = new AsyncDataHolder(
                    (String) ((JSONArray) asyncInfo.get(i)).get(0),
                    (String) ((JSONArray) asyncInfo.get(i)).get(1),
                    (String) ((JSONArray) asyncInfo.get(i)).get(2));

            asyncConnections.add(newData);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> create(int num, String model, Map<String, Object> modelParams) {
        if (currentState != State.INITIALIZED && currentState != State.MODELS_ADDED) {
            throw new IllegalStateException("create called before init");
        }
        currentState = State.MODELS_ADDED;

        System.out.println(GSON.toJson(modelParams));
        if (modelParams.containsKey("asyncInfo")) {
            addAsyncInfo(modelParams);
        }

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < num; i++) {
            HashMap<String, Object> localParams = new HashMap<>(modelParams);

            String eid = this.eidPrefix + (this.idCounter + i);
            localParams.put(ENTITY_ID_KEY, eid);
            localParams.put(ENTITY_MODEL_KEY, model);
            this.simulationController.addModel(model, localParams);

            //data to send back to mosaik
            JSONObject entity = new JSONObject();
            entity.put("eid", eid);
            entity.put("type", model);
            entity.put("rel", new JSONArray());
            jsonArray.add(entity);
            this.entities.put(eid, this.idCounter + i);
        }
        this.idCounter += num;
        return jsonArray;
    }

    /**
     * Parameters to add to the simulator meta model
     *
     * @param simParams passed by Mosaik init function
     * @return list of params to add to the meta model
     */
    protected List<String> paramsToAdd(Map<String, Object> simParams) {
        if (!simParams.containsKey("__additionalParams__")) {
            return Collections.emptyList();
        }
        String paramsString = (String) simParams.get("__additionalParams__");
        JSONArray parsedArgs = (JSONArray) JSONValue.parse(paramsString);
        return jsonArrayToList(parsedArgs);
    }

    protected void addFrameworkDataToMetadata(FrameworkMetaBuilder metaBuilder, Map<String, Object> simParams, String modelName) {
        List<String> addParams = paramsToAdd(simParams);
        List<String> addAttr = attributesToAdd(simParams);
        metaBuilder.addParamsToModel(modelName, addParams);
        metaBuilder.addAttrsToModel(modelName, addAttr);
    }

    private List<String> jsonArrayToList(JSONArray array) {
        assert array != null;
        List<String> result = new ArrayList<>(array.size());
        for (Object anArray : array) {
            result.add((String) anArray);
        }
        return result;
    }

    /**
     * Attributes to add to the simulator meta model
     *
     * @param simParams passed by Mosaik create function
     * @return list of attributes to add to the meta model
     */
    protected List<String> attributesToAdd(Map<String, Object> simParams) {
        if (!simParams.containsKey("__additionalInputs__")) {
            return Collections.emptyList();
        }
        String paramsString = (String) simParams.get("__additionalInputs__");
        JSONArray parsedArgs = (JSONArray) JSONValue.parse(paramsString);
        return jsonArrayToList(parsedArgs);
    }

    @Override
    public long step(long time, Map<String, Object> inputs) throws Exception {
        this.addInputsToCollection(inputs);
        this.simulationController.step(time);
        this.sendAllAsyncOutputs(inputs);
        return time + this.stepSize;
    }

    @Override
    public Map<String, Object> getData(Map<String, List<String>> outputs) {
        Map<String, Object> data = new HashMap<>();
        //*outputs* lists the models and the output values that are requested
        //go through entities in outputs
        for (Map.Entry<String, List<String>> entity : outputs.entrySet()) {
            String eid = entity.getKey();
            List<String> attrs = entity.getValue();
            HashMap<String, Object> values = new HashMap<>();
            int idx = this.entities.get(eid);
            //go through attrs of the entity
            for (String attr : attrs) {
                values.put(attr, this.simulationController.getValue(idx, attr));
            }
            data.put(eid, values);
        }
        return data;
    }

    /**
     * Sends asynchronous data based on current iteration inputs
     *
     * @param inputs iteration inputs
     */
    protected void sendAllAsyncOutputs(Map<String, Object> inputs) {
        for (AsyncDataHolder data : asyncConnections) {
            sendAsyncOutput(
                    inputs,
                    data.getOutputName(),
                    data.getDestinationInputName(),
                    data.getSynchroniseName());
        }
    }

    /**
     * Sends ASYNCHRONOUS data to another simulator
     *
     * @param inputs               Mosaik inputs
     * @param thisOutputName       string key from this simulator, its value will be sent to @param destinationInputName
     * @param destinationInputName name of input on receiving end
     * @param synchroniseName      used for synchronization (which end entity to target), follow naming scheme
     */
    protected Map<String, Object> sendAsyncOutput(Map<String, Object> inputs, String thisOutputName, String destinationInputName, String synchroniseName) {
        Map<String, Object> commands = prepareASyncData(inputs, thisOutputName, destinationInputName, synchroniseName);
        try {
            System.out.println(GSON.toJson(commands));
            getMosaik().setData(commands);
            return commands;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> prepareASyncData(Map<String, Object> inputs, String thisOutputName, String destinationInputName, String synchroniseName) {
        //async data processing
        //get data to return ASYNC
        Map<String, Object> commands = new HashMap<>();
        //  for agent_eid, attrs in inputs.items():
        for (Map.Entry<String, Object> inputsEntrySet : inputs.entrySet()) {
            String thisAgentEid = inputsEntrySet.getKey();
            Map<String, Object> inputValuesMap = (Map<String, Object>) inputsEntrySet.getValue();
            Map<String, Object> asyncSources = (HashMap<String, Object>) inputValuesMap.get(synchroniseName);

            if (asyncSources == null) {
                continue; //this was intended for another input, skip it
            }

            //for model_eid, value in values.items():
            for (Map.Entry<String, Object> asyncSource : asyncSources.entrySet()) {
                String modelEid = asyncSource.getKey();

                int thisIdx = this.entities.get(thisAgentEid);
                Object actionOutput = this.simulationController.getValue(thisIdx, thisOutputName);

                Map<String, Object> inputValues = new HashMap<>();
                inputValues.put(destinationInputName, actionOutput); //destinationInputName on target simulator
                Map<String, Object> target = new HashMap<>();
                target.put(modelEid, inputValues);
                commands.put(thisAgentEid, target);
            }
        }
        return commands;
    }

    @Override
    public void cleanup() throws Exception {
        super.cleanup();
        currentState = State.FINISHED;
    }

    protected String getEidPrefix() {
        return eidPrefix;
    }

    protected String getSimulationId() {
        return simulationId;
    }

    protected State getCurrentState() {
        return currentState;
    }

    /**
     * Adds Mosaik received inputs during Simulator.step() to the currently attached simulator
     * Values are added into a Map with String keys (name of input).
     * Only one value can be added per key, first received value is extracted,
     * if more than one is received an exception is thrown
     *
     * @param inputs Mosaik inputs from step()
     */
    @SuppressWarnings("unchecked")
    protected void addInputsToCollection(Map<String, Object> inputs) {
        //go through entities in inputs
        for (Map.Entry<String, Object> entity : inputs.entrySet()) {
            //get attrs from entity
            Map<String, Object> attrs = (Map<String, Object>) entity.getValue();
            //go through attrs of the entity
            for (Map.Entry<String, Object> attr : attrs.entrySet()) {
                String eid = entity.getKey();
                if (!this.entities.containsKey(eid)) {
                    throw new IllegalStateException("Could not find eid: " + eid);
                }
                int idx = this.entities.get(eid);
                Collection inputValues = ((JSONObject) attr.getValue()).values();
                if (inputValues.size() > 1) {
                    throw new IllegalStateException("More than 1 input values received for: " + eid + " - " + idx + " ;; param: " + attr.getKey());
                }
                this.simulationController.setValue(idx, attr.getKey(), inputValues.iterator().next()); //write entire Map for given entity
            }
        }
    }

    /**
     * Get current step size
     *
     * @return step size
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * Returns current simulation controller
     *
     * @return AbstractSimulationController
     */
    protected AbstractSimulationController getSimulationController() {
        return simulationController;
    }

    /**
     * Returns the map of all async connections, this represents the inverse data flow topology
     *
     * @return async connections
     */
    protected List<AsyncDataHolder> getAsyncConnections() {
        return Collections.unmodifiableList(asyncConnections);
    }

    private enum State {
        NEW,
        INITIALIZED,
        MODELS_ADDED,
        FINISHED
    }
}
