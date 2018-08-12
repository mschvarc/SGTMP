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

package testframework.mosaik.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.MeasureAttribute;
import testframework.mosaik.measure.MeasureConverter;
import testframework.mosaik.dto.measure.MeasureType;
import testframework.mosaik.dto.measure.PerStepMeasure;
import testframework.mosaik.dto.measure.operators.ExpectedResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static testframework.mosaik.models.ModelTestEvaluator.TEST_RESULT_VAR_NAME;


public class MeasureConvertorTest {

    private final String GlobalMeasureIdMapping = getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json");
    private final String GlobalTargetMeasures = getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json");
    private final int MeasuresCount = 2;

    private final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void MeasureConverterTest() throws Exception {
        MeasureConverter converter = new MeasureConverter();
        Map<Integer, MeasureAttribute> measureAttributeMappings = converter.getMeasureAttributeMappings(GlobalMeasureIdMapping);

        Map<Long, PerStepMeasure> measuresMap = converter.getMeasuresMap(measureAttributeMappings, GlobalTargetMeasures);

        Map<MeasureAttribute, ExpectedResult> step1Measures = measuresMap.get(1L).getMeasureAttributes();
        Map<MeasureAttribute, ExpectedResult> step20Measures = measuresMap.get(20L).getMeasureAttributes();

        Measure fifty = new Measure(50);
        for (Map.Entry<MeasureAttribute, ExpectedResult> entry : step1Measures.entrySet()) {
            assertThat(entry.getValue().isInRange(fifty)).isTrue();
        }
        for (Map.Entry<MeasureAttribute, ExpectedResult> entry : step20Measures.entrySet()) {
            assertThat(entry.getValue().isInRange(fifty)).isFalse();
        }
    }

    @Test
    public void MeasureconvertorIntegration() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("__GlobalMeasureIdMapping__", GlobalMeasureIdMapping);
        map.put("__GlobalTargetMeasures__", GlobalTargetMeasures);
        map.put("__InputCount__", 0);
        map.put("__MeasuresCount__", MeasuresCount);
        map.put("__PermanentFailureEntry__", false);

        ModelGlobalTestEvaluator model = new ModelGlobalTestEvaluator(map);

        MeasureAttribute measureAttribute1 = new MeasureAttribute();
        measureAttribute1.setSimulatorId("Simulator1");
        measureAttribute1.setModelId("Model1");
        measureAttribute1.setName("Simulator1:Model1:measureValue1");
        measureAttribute1.setMeasureInputName("name1");

        MeasureAttribute measureAttribute2 = new MeasureAttribute();
        measureAttribute2.setSimulatorId("Simulator1");
        measureAttribute2.setModelId("Model1");
        measureAttribute2.setName("Simulator1:Model1:measureValue2");
        measureAttribute2.setMeasureInputName("name2");

        Measure name1 = new Measure();
        name1.setMeasureType(MeasureType.ACTUAL);
        name1.setValue(40);
        name1.setMeasureAttribute(measureAttribute1);
        name1.setUnit("miliVolts");

        Measure name2 = new Measure();
        name2.setMeasureType(MeasureType.ACTUAL);
        name2.setValue(150);
        name2.setMeasureAttribute(measureAttribute2);

        model.setValue("name1", mapper.writeValueAsString(name1));
        model.setValue("name2", mapper.writeValueAsString(name2));

        model.step(10);
        Assert.assertTrue((boolean) model.getValue(TEST_RESULT_VAR_NAME));

        model.step(20);
        Assert.assertTrue((boolean) model.getValue(TEST_RESULT_VAR_NAME));
    }

    private String getResourceContent(String strPath) {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            return IOUtils.toString(classLoader.getResourceAsStream(strPath), Charset.forName("UTF-8"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
