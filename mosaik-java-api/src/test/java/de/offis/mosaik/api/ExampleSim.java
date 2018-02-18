package de.offis.mosaik.api;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleSim extends Simulator {

    // Alternatively, put all the JSON into a .json file and read meta data from
    // that.
    private static final JSONObject meta = (JSONObject) JSONValue.parse(("{"
            + "    'api_version': '" + Simulator.API_VERSION + "',"
            + "    'models': {"
            + "        'ExampleModel': {" + "            'public': true,"
            + "            'params': ['init_val'],"
            + "            'attrs': ['val', 'delta']" + "        }"
            + "    }" + "}").replace("'", "\""));
    private final HashMap<String, JExampleModel> instances;
    private int stepSize = 60;
    private int idCounter = 0;

    public ExampleSim() {
        super("ExampleSim");
        this.instances = new HashMap<>();
    }

    public static void main(String[] args) throws Throwable {
        Simulator sim = new ExampleSim();
        //TODO: Implement command line arguments parser (http://commons.apache.org/proper/commons-cli/)
        if (args.length < 1) {
            String ipaddr[] = {"127.0.0.1:5678"};
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
        return ExampleSim.meta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> create(int num, String model,
                                            Map<String, Object> modelParams) {
        JSONArray entities = new JSONArray();
        for (int i = 0; i < num; i++) {
            String eid = "EM_" + (this.idCounter + i);
            if (modelParams.containsKey("init_val")) {
                float initVal = ((Number) modelParams.get("init_val")).floatValue();
                this.instances.put(eid, new JExampleModel(initVal));
            } else {
                this.instances.put(eid, new JExampleModel());
            }

            JSONObject entity = new JSONObject();
            entity.put("eid", eid);
            entity.put("type", model);
            entity.put("rel", new JSONArray());
            entities.add(entity);
        }
        this.idCounter += num;
        return entities;
    }

    @Override
    public void setupDone() throws Exception {
    }

    @SuppressWarnings("unchecked")
    @Override
    public long step(long time, Map<String, Object> inputs) {
        //TODO input handling not implemented yet!
        for (Map.Entry<String, Object> entity : inputs.entrySet()) {
            String eid = entity.getKey();
            Map<String, Object> attrs = (Map<String, Object>) entity.getValue();

            for (Map.Entry<String, Object> attr : attrs.entrySet()) {
                String attrName = attr.getKey();
                System.out.println(attrName);
                if (attrName.equals("delta")) {
                } else {
                    continue;
                }
                Object[] values = ((Map<String, Object>) attr.getValue()).values().toArray();
                float value = 0;
                for (Object value1 : values) {
                    value += ((Number) value1).floatValue();
                }

                // Get model instance and update it
                JExampleModel instance = this.instances.get(eid);
                instance.set_delta(value);
                System.out.println("setDelta");
            }
        }

        for (JExampleModel instance : this.instances.values()) {
            instance.step();
        }

        return time + this.stepSize;
    }

    @Override
    public Map<String, Object> getData(Map<String, List<String>> outputs) {
        Map<String, Object> data = new HashMap<>();

        for (Map.Entry<String, List<String>> entity : outputs.entrySet()) {
            String eid = entity.getKey();
            List<String> attrs = entity.getValue();
            HashMap<String, Object> values = new HashMap<>();
            JExampleModel instance = this.instances.get(eid);

            for (String attr : attrs) {
                if (attr.equals("val")) {
                    values.put(attr, instance.get_val());
                } else if (attr.equals("delta")) {
                    values.put(attr, instance.get_delta());
                }
            }
            data.put(eid, values);
        }
        return data;
    }
}

class JExampleModel {
    private float val;
    private float delta = 1;

    public JExampleModel() {
        this.val = 0;
    }

    public JExampleModel(float initVal) {
        this.val = initVal;
    }

    public float get_val() {
        return this.val;
    }

    public float get_delta() {
        return this.delta;
    }

    public void set_delta(float delta) {
        this.delta = delta;
    }

    public void step() {
        this.val += this.delta;
    }
}

