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

package testframework.mosaik.example.models;

import testframework.mosaik.models.AbstractModel;

import java.util.Map;
import java.util.Random;

import static testframework.mosaik.util.Helpers.getDouble;


public class Model2 extends AbstractModel {
    private float value;
    private float input;
    private long step;
    private double sim1ValIN;

    public Model2() {
        this.value = 0;
        this.input = 0;
    }

    public Model2(Map<String, Object> params) {
        this();
        this.value = (float) getDouble(params.get("init_val"));
    }

    public Model2(float initialValue) {
        this();
        this.value = initialValue;
    }

    @Override
    public void synchronizeFieldsToMap() {
        setValue("value", value);
        setValue("step", step);
    }

    @Override
    public void synchronizeMapToFields() {
        sim1ValIN = getDouble(getValue("sim1ValIN"));
    }

    public void setInput(float inp) {
        this.input = inp;
    }

    public float getVal() {
        return this.value;
    }

    public void step(long time) {
        synchronizeMapToFields();
        this.value = ((new Random()).nextFloat() * 0.5f) + input;

        float sim1DeltaOUT = 0f;
        float multiplier = 0.5f;
        if (sim1ValIN > 0.49f) {
            sim1DeltaOUT = -1f * multiplier;
        } else if (sim1ValIN < -0.49f) {
            sim1DeltaOUT = +1f * multiplier;
        }
        this.setValue("sim1DeltaOUT", sim1DeltaOUT);
        step = time;

        synchronizeFieldsToMap();
    }

}
