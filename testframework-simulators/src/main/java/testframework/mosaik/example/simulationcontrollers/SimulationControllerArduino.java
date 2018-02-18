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

package testframework.mosaik.example.simulationcontrollers;

import testframework.mosaik.example.models.ModelArduino;
import testframework.mosaik.models.Model;
import testframework.mosaik.models.factory.ModelFactory;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;


public class SimulationControllerArduino extends AbstractSimulationController {

    public SimulationControllerArduino(ModelFactory modelFactory) {
        super(modelFactory);
    }


    @Override
    public void step(long time) {
        //parallel for, use this when simulating >1 arduinos
        getModels().parallelStream().forEach(model -> model.step(time));
    }


    @Override
    public void cleanup() {
        for (Model model1 : getModels()) {
            ModelArduino model = (ModelArduino) model1;
            model.disconnect();
        }
        System.out.println("Cleaned up Arduinos");
        super.cleanup();
    }

}
