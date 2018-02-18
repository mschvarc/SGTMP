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

import testframework.mosaik.example.models.Model2;
import testframework.mosaik.example.models.factory.Model2Factory;
import testframework.mosaik.simulationcontrollers.AbstractSimulationController;


public class SimulationController2 extends AbstractSimulationController {

    public SimulationController2(Model2Factory modelFactory) {
        super(modelFactory);
    }


    public double getVal(int idx) {
        Model2 model = (Model2) this.getModels().get(idx);
        return model.getVal();
    }

    public void setInput(int idx, float input) {
        Model2 model = (Model2) this.getModels().get(idx);
        model.setInput(input);
    }


}
