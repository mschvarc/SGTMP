package de.offis.mosaik.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExampleMAS extends Simulator {

    // Alternatively, put all the JSON into a .json file and read meta data from
    // that.
    private static final JSONObject meta = (JSONObject) JSONValue.parse(("{"
            + "    'api_version': '" + Simulator.API_VERSION + "',"
            + "    'models': {"
            + "        'ExampleAgent': {" + "            'public': true,"
            + "            'params': []," + "            'attrs': []"
            + "        }" + "    }" + "}").replace("'", "\""));
    private int stepSize = 1;
    private ExampleAgent agent;

    public ExampleMAS() {
        super("ExampleMAS");
        this.agent = null;
    }

    public static void main(String[] args) throws Throwable {
        final Simulator sim = new ExampleMAS();
        //TODO: Implement command line arguments parser (http://commons.apache.org/proper/commons-cli/)
        if (args.length < 1) {
            String ipaddr[] = {"localhost:5555"};
            SimProcess.startSimulation(ipaddr, sim);
        } else {
            SimProcess.startSimulation(args, sim);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> init(String sid, Map<String, Object> simParams) {
        if (simParams.containsKey("step_size")) {
            this.stepSize = ((Number) simParams.get("step_size")).intValue();
        }
        return ExampleMAS.meta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> create(int num, String model,
                                            Map<String, Object> modelParams) {
        if (this.agent != null || num > 1) {
            throw new RuntimeException("Can only create one agent. :-(");
        }

        this.agent = new ExampleAgent();

        JSONObject entity = new JSONObject();
        entity.put("eid", "EA_0");
        entity.put("type", model);
        entity.put("rel", new JSONArray());

        JSONArray entities = new JSONArray();
        entities.add(entity);

        return entities;
    }

    @Override
    public void setupDone() throws Exception {
    }

    @Override
    public long step(long time, Map<String, Object> inputs) throws Exception {
        MosaikProxy mosaik = this.getMosaik();

        // Get simulation progress in percent
        float progress = mosaik.getProgress();
        System.out.println("Progress: " + progress);

        // Show various ways of getting related entities
        Map<String, Object> rels = mosaik.getRelatedEntities();
        System.out.println("All relations: " + rels);
        rels = mosaik.getRelatedEntities("eid0");
        System.out.println("Relations for eid0: " + rels);
        rels = mosaik.getRelatedEntities(new String[]{"eid0", "eid1"});
        System.out.println("Relations for {eid0, eid1}: " + rels);

        // Get another simulator's data from mosaik
        HashMap<String, List<String>> attrs = new HashMap<>();
        attrs.put("eid0", Arrays.asList(new String[]{"a"}));
        Map<String, Object> data = mosaik.getData(attrs);

        // Set input data for another simulator
        HashMap<String, Object> inputData = new HashMap<>();
        inputData.put("EA_0", data);
        mosaik.setData(inputData);

        this.agent.step();

        return time + this.stepSize;
    }

    @Override
    public Map<String, Object> getData(Map<String, List<String>> outputs) {
        // Nothing to give ...
        Map<String, Object> data = new HashMap<>();
        return data;
    }
}

class ExampleAgent {
    public ExampleAgent() {
    }

    public void step() {
    }
}
