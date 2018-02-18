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

import testframework.mosaik.example.models.Model1;
import testframework.mosaik.example.models.factory.Model1Factory;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;


public class SimulationController1 extends AbstractSimulationController {

    public SimulationController1(Model1Factory modelFactory) {
        super(modelFactory);
    }

    public double getVal(int idx) {
        Model1 model = (Model1) this.getModels().get(idx);
        return model.getVal();
    }

    public double getDelta(int idx) {
        Model1 model = (Model1) this.getModels().get(idx);
        return model.getDelta();
    }

    public void setDelta(int idx, double delta) {
        Model1 model = (Model1) this.getModels().get(idx);
        model.setDelta(delta);
    }
}
