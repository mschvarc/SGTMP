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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import testframework.mosaik.dto.measure.JsonTestStep;
import testframework.mosaik.dto.measure.Measure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelGlobalTestEvaluatorTestCase {

    private final Gson GSON = new Gson();
    private final ObjectMapper mapper = new ObjectMapper();


    private ModelGlobalTestEvaluator model;
    private Map<String, Object> modelParams;

    @Before
    public void setup() {
        modelParams = new HashMap<>();
    }


    @Test
    public void testNoInputPass() {
        modelParams.put("__InputCount__", 0);
        modelParams.put("__MeasuresCount__", 0);
        modelParams.put("__PermanentFailureEntry__", false);
        model = new ModelGlobalTestEvaluator(modelParams);
        model.step(1);
        assertThat(model.isPassed()).isEqualTo(true);
        assertThat(model.getMessages()).isNullOrEmpty();
    }

    @Test
    public void testMeasures() throws Exception {
        modelParams.put("__InputCount__", 0);
        modelParams.put("__MeasuresCount__", 1);
        modelParams.put("__PermanentFailureEntry__", false);
        modelParams.put("__GlobalMeasureIdMapping__", getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json"));
        modelParams.put("__GlobalTargetMeasures__", getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json"));
        model = new ModelGlobalTestEvaluator(modelParams);
        //x>20 && x< 100
        Measure measure1a = new Measure(150);
        Measure measure1b = new Measure(50);

        model.setValue("name1", mapper.writeValueAsString(measure1a));
        model.step(1);
        assertThat(model.isPassed()).isFalse();
        assertThat(model.getMessages()).isNullOrEmpty();

        model.setValue("name1", mapper.writeValueAsString(measure1b));
        model.step(1);
        assertThat(model.isPassed()).isTrue();
        assertThat(model.getMessages()).isNullOrEmpty();
    }

    @Test
    public void testMultipleCreation() throws Exception {

    }

    @Test
    public void measureSerializationTest() throws Exception {
        modelParams.put("__InputCount__", 0);
        modelParams.put("__MeasuresCount__", 0);
        modelParams.put("__PermanentFailureEntry__", false);
        modelParams.put("__GlobalMeasureIdMapping__", getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json"));
        modelParams.put("__GlobalTargetMeasures__", getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json"));
        model = new ModelGlobalTestEvaluator(modelParams);
        //x>20 && x< 100
        Measure measure1a = new Measure(150);

        model.setValue("name1", mapper.writeValueAsString(measure1a));
        model.step(1);
        Map<String, String> lastKV = model.lastSimulationStep();

        TypeReference<Map<Long, JsonTestStep>> typeRef = new TypeReference<Map<Long, JsonTestStep>>() {
        };

        JsonTestStep expected = new JsonTestStep();
        expected.addObservedMeasure(new Measure(150));

        String allMeasuresString = lastKV.get("allMeasures");
        Map<Long, JsonTestStep> results = mapper.readValue(allMeasuresString, typeRef);
        assertThat(results).containsKey(1L);
        assertThat(results).containsValues(expected);


    }

    private String getResourceContent(String strPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream(strPath), Charset.forName("UTF-8"));
    }

}
