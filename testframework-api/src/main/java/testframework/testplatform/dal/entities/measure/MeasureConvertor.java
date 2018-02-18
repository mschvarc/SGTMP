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

package testframework.testplatform.dal.entities.measure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MeasureConvertor {

    private final ObjectMapper mapper = new ObjectMapper();

    public MeasureConvertor() {

        Hibernate5Module hibernate5Module = new Hibernate5Module();
        hibernate5Module.configure(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION, true);
        mapper.registerModule(hibernate5Module);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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
