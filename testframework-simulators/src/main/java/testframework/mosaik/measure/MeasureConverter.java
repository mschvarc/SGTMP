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

package testframework.mosaik.measure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import testframework.mosaik.dto.measure.JsonTestStep;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.MeasureAttribute;
import testframework.mosaik.dto.measure.PerStepMeasure;
import testframework.mosaik.dto.measure.PerStepMeasureSerial;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasureConverter {

    private final ObjectMapper mapper = new ObjectMapper();

    public MeasureConverter() {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public Map<Integer, MeasureAttribute> getMeasureAttributeMappings(String json) throws IOException {
        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyMap();
        }

        TypeReference<List<MeasureAttribute>> typeRef = new TypeReference<List<MeasureAttribute>>() {
        };

        List<MeasureAttribute> values = mapper.readValue(json, typeRef);
        Map<Integer, MeasureAttribute> map = new HashMap<>();
        for (int i = 0; i < values.size(); i++) {
            map.put(i, values.get(i));
        }
        return map;
    }

    public Measure getMeasure(String json) throws IOException {
        return mapper.readValue(json, Measure.class);
    }

    public List<Measure> getFinalResults(Map<Integer, Measure> receivedMeasures) {
        return new ArrayList<>(receivedMeasures.values());
    }

    public String getFinalResultsJson(Map<Integer, Measure> receivedMeasures) throws JsonProcessingException {
        return mapper.writeValueAsString(getFinalResults(receivedMeasures));
    }

    public String getAllFinalResultsJson(Map<Long, JsonTestStep> steps) throws JsonProcessingException {
        return mapper.writeValueAsString(steps);
    }

    private List<PerStepMeasureSerial> getMeasuresList(String globalTargetMeasures) throws IOException {

        JSONArray jsonArray = (JSONArray) JSONValue.parse(globalTargetMeasures);
        List<PerStepMeasureSerial> expectedResultArray = new ArrayList<>(jsonArray.size());

        for (Object o : jsonArray) {
            String inner = (String) o;
            PerStepMeasureSerial expectedResult = mapper.readValue(inner, PerStepMeasureSerial.class);
            expectedResultArray.add(expectedResult);
        }

        return expectedResultArray;
    }

    public Map<Long, PerStepMeasure> getMeasuresMap(Map<Integer, MeasureAttribute> measuresIdMapping, String globalTargetMeasures) throws IOException {
        if (measuresIdMapping == null) {
            throw new IllegalArgumentException("measuresIdMapping is null");
        }
        if (Strings.isNullOrEmpty(globalTargetMeasures)) {
            return Collections.emptyMap();
        }

        Map<Long, PerStepMeasure> result = new HashMap<>();

        List<PerStepMeasureSerial> measuresList = getMeasuresList(globalTargetMeasures);

        for (PerStepMeasureSerial perStepMeasureSerial : measuresList) {
            PerStepMeasure perStepMeasure = perStepMeasureSerial.toPerStepMeasure();
            result.put(perStepMeasure.getStep(), perStepMeasure);
        }

        return result;
    }

}
