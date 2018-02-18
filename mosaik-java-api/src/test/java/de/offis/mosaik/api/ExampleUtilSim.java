package de.offis.mosaik.api;

import de.offis.mosaik.api.utils.AttributeInput;
import de.offis.mosaik.api.utils.DataBuilder;
import de.offis.mosaik.api.utils.DataRequest;
import de.offis.mosaik.api.utils.EntityDescription;
import de.offis.mosaik.api.utils.EntityDescriptionBuilder;
import de.offis.mosaik.api.utils.InputForDestinationEntity;
import de.offis.mosaik.api.utils.InputsFromSourceEntity;
import de.offis.mosaik.api.utils.MetaBuilder;
import de.offis.mosaik.api.utils.ModelParams;
import de.offis.mosaik.api.utils.SimParams;
import de.offis.mosaik.api.utils.StepInputs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExampleUtilSim extends Simulator {

    private final HashMap<String, JUtilExampleModel> instances;
    private int stepSize = 60;
    private String simulatorID;

    public ExampleUtilSim() {
        super("ExampleSim");
        instances = new HashMap<>();
    }

    public static void main(String[] args) throws Throwable {
        Simulator sim = new ExampleUtilSim();
        if (args.length < 1) {
            String ipaddr[] = {"127.0.0.1:5678"};
            SimProcess.startSimulation(ipaddr, sim);
        } else {
            SimProcess.startSimulation(args, sim);
        }
    }

    @Override
    public Map<String, Object> init(String sid, Map<String, Object> simParams)
            throws Exception {
        SimParams params = new SimParams(simParams);
        this.simulatorID = sid;
        if (params.containsKey("step_size")) {
            this.stepSize = ((Number) simParams.get("step_size")).intValue();
        }
        MetaBuilder mBuilder = new MetaBuilder();
        mBuilder.addModel("ExampleModel", true,
                new String[]{"init_val"}, new String[]{"val", "delta"});
        return mBuilder.getOutput();
    }

    @Override
    public List<Map<String, Object>> create(int num, String model,
                                            Map<String, Object> modelParams) throws Exception {
        ModelParams params = new ModelParams(modelParams);
        EntityDescriptionBuilder eBuilder = new
                EntityDescriptionBuilder(this.simulatorID);
        for (int i = 0; i < num; i++) {
            EntityDescription entity = eBuilder
                    .addEntityDescription("ExampleModel", null);
            if (params.containsKey("init_val")) {
                float initVal = ((Number) modelParams
                        .get("init_val")).floatValue();
                this.instances.put(entity.getEID(), new
                        JUtilExampleModel(initVal));
            } else {
                this.instances.put(entity.getEID(), new
                        JUtilExampleModel());
            }
        }
        return eBuilder.getOutput();
    }

    @Override
    public void setupDone() throws Exception {
        System.out.println("Setup Done!");
    }

    @Override
    public long step(long time, Map<String, Object> inputs) throws Exception {
        if (inputs != null) {
            StepInputs stepInputs = new StepInputs(inputs);

            for (InputForDestinationEntity inputDest : stepInputs) {
                String destEID = inputDest.getDestinatonEid();

                for (AttributeInput attrInput : inputDest) {

                    if (attrInput.getAttributeName().equals("delta")) {
                        float delta = 0;

                        for (InputsFromSourceEntity inputSrc : attrInput) {
                            delta += ((Number) inputSrc.getValue())
                                    .floatValue();
                        }
                        JUtilExampleModel instance = this.instances
                                .get(destEID);
                        instance.set_delta(delta);
                    }
                }
            }
        }


        for (JUtilExampleModel instance : this.instances.values()) {
            instance.step();
        }

        return time + stepSize;
    }

    @Override
    public Map<String, Object> getData(Map<String, List<String>> outputs)
            throws Exception {
        DataRequest request = new DataRequest(outputs);
        DataBuilder dBuilder = new DataBuilder();

        for (Map.Entry<String, List<String>> entry : request.entrySet()) {
            String requestedEID = entry.getKey();
            JUtilExampleModel model = this.instances.get(requestedEID);
            for (String attr : entry.getValue()) {
                if (attr.equals("val")) {
                    dBuilder.addEntry(requestedEID, attr, model.get_val());
                } else if (attr.equals("delta")) {
                    dBuilder.addEntry(requestedEID, attr, model.get_delta());
                }
            }
        }

        return dBuilder.getOutput();
    }

}

class JUtilExampleModel {
    private float val;
    private float delta = 1;

    public JUtilExampleModel() {
        this.val = 0;
    }

    public JUtilExampleModel(float initVal) {
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
