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

import com.fasterxml.jackson.databind.ObjectMapper;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.MeasureAttribute;
import testframework.mosaik.dto.measure.MeasureType;
import testframework.mosaik.models.AbstractModel;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static testframework.mosaik.simulators.AbstractSimulator.MODEL_INDEX_KEY;
import static testframework.mosaik.util.Helpers.getDouble;
import static testframework.mosaik.util.Helpers.getInt;

/**
 * DataModel for Simulation
 * Actual simulation logic done in step()
 */
public class Model1 extends AbstractModel {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Random random = new Random();
    private double val;
    private double delta = 0;
    private long step = 0;
    private Measure name1 = new Measure(50);
    private Measure name2 = new Measure(150);


    public Model1() {
        //Example demonstrating how to setup measures
        this.val = 0;
        MeasureAttribute measureAttribute1 = new MeasureAttribute();
        measureAttribute1.setSimulatorId("Simulator1");
        measureAttribute1.setModelId("Model1");
        measureAttribute1.setName("Simulator1:Model1:measureValue1");
        measureAttribute1.setMeasureInputName("name1");

        name1.setUnit("unit1");
        name1.setMeasureAttribute(measureAttribute1);
        name1.setMeasureType(MeasureType.ACTUAL);

        MeasureAttribute measureAttribute2 = new MeasureAttribute();
        measureAttribute2.setSimulatorId("Simulator1");
        measureAttribute2.setModelId("Model1");
        measureAttribute2.setName("Simulator1:Model1:measureValue2");
        measureAttribute2.setMeasureInputName("name2");
        name1.setUnit("unit2");
        name2.setMeasureAttribute(measureAttribute2);
        name2.setMeasureType(MeasureType.ACTUAL);

    }

    public Model1(Map<String, Object> params) {
        this();
        this.val = (float) getDouble(params.get("init_val"));
        int index = params.get(MODEL_INDEX_KEY) != null ? getInt(params.get(MODEL_INDEX_KEY)) : 0;
        this.name1.getMeasureAttribute().setModelIndex(index);
        this.name2.getMeasureAttribute().setModelIndex(index);

        synchronizeFieldsToMap();
    }

    public Model1(float initVal) {
        this();
        this.val = initVal;
    }

    @Override
    public void synchronizeFieldsToMap() {
        setValue("val", val);
        setValue("delta", delta);
        setValue("step", step);

        try {
            setValue("name1", mapper.writeValueAsString(name1));
            setValue("name2", mapper.writeValueAsString(name2));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public void synchronizeMapToFields() {
        delta = getDouble(getValue("delta"));
        val = getDouble(getValue("val"));
    }

    public double getVal() {
        return this.val;
    }

    public double getDelta() {
        return this.delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    //EACH STEP -> val+= delta
    public void step(long time) {
        synchronizeMapToFields();
        this.val += this.delta;
        step = time;

        name1.setValue(random.nextInt(500));
        name2.setValue(random.nextInt(100));

        synchronizeFieldsToMap();
    }
}
