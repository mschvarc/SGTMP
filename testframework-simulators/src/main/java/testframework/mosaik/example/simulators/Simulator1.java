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

package testframework.mosaik.example.simulators;

import de.offis.mosaik.api.SimProcess;
import de.offis.mosaik.api.Simulator;
import testframework.mosaik.example.models.factory.Model1Factory;
import testframework.mosaik.example.simulationcontrollers.SimulationController1;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;
import testframework.mosaik.simulators.AbstractSimulator;
import testframework.mosaik.util.FrameworkMetaBuilder;

import java.util.Map;

public final class Simulator1 extends AbstractSimulator {
    private static final String MODEL_NAME = "Model1";

    private final FrameworkMetaBuilder metaBuilder;

    public Simulator1(AbstractSimulationController prototype) {
        super(prototype, "SimulationController1");
        metaBuilder = new FrameworkMetaBuilder();
        metaBuilder.addModel(MODEL_NAME, true,
                new String[]{
                        "init_val"
                },
                new String[]{
                        "val", "delta", "step"
                });
    }

    public static void main(String[] args) throws Throwable {
        Simulator sim = new Simulator1(new SimulationController1(new Model1Factory()));
        if (args.length <= 1) {
            throw new IllegalArgumentException();
        } else {
            SimProcess.startSimulation(args, sim);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> init(String sid, Map<String, Object> simParams) {
        super.init(sid, simParams);
        addFrameworkDataToMetadata(metaBuilder, simParams, MODEL_NAME);
        return this.metaBuilder.getOutput();
    }


    @SuppressWarnings("unchecked")
    @Override
    public long step(long time, Map<String, Object> inputs) {
        addInputsToCollection(inputs);
        getSimulationController().step(time);
        sendAllAsyncOutputs(inputs);
        return time + getStepSize();
    }

}
