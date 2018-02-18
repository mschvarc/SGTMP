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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.log4j.Logger;
import testframework.mosaik.dto.measure.JsonTestStep;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.MeasureAttribute;
import testframework.mosaik.measure.MeasureConverter;
import testframework.mosaik.dto.measure.PerStepMeasure;
import testframework.mosaik.dto.measure.operators.ExpectedResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static testframework.mosaik.util.Helpers.getBool;
import static testframework.mosaik.util.Helpers.getInt;


public class ModelGlobalTestEvaluator extends AbstractModel implements ModelTestEvaluator {

    private static final long FINAL_STEP_TIME = -1;
    private static final Logger logger = Logger.getLogger(ModelGlobalTestEvaluator.class);
    private final Map<Long, JsonTestStep> collectedMeasures = new HashMap<>();
    private boolean testPassed = true;
    private StringBuilder messages = new StringBuilder();
    private int inputCount = -1;
    private int measuresCount = -1;
    private boolean permanentFailureCondition = false;
    private Map<Long, PerStepMeasure> perStepMeasuresTarget;
    private Map<Integer, MeasureAttribute> measuresIdMapping;
    private MeasureConverter measureConvertor = new MeasureConverter();
    private Map<Integer, Measure> receivedMeasuresCurrentStep = new HashMap<>();


    public ModelGlobalTestEvaluator(Map<String, Object> modelParams) {
        super(modelParams);
        try {
            inputCount = getInt(modelParams.get("__InputCount__"));
            measuresCount = getInt(modelParams.get("__MeasuresCount__"));
            permanentFailureCondition = getBool(modelParams.get("__PermanentFailureEntry__"));
            measuresIdMapping = measureConvertor.getMeasureAttributeMappings((String) modelParams.get("__GlobalMeasureIdMapping__"));
            perStepMeasuresTarget = measureConvertor.getMeasuresMap(measuresIdMapping, (String) modelParams.get("__GlobalTargetMeasures__"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        synchronizeFieldsToMap();
    }

    @Override
    public boolean isPassed() {
        return testPassed;
    }

    @Override
    public String getMessages() {
        return messages.toString();
    }

    @Override
    public void step(long time) {
        synchronizeMapToFields();

        boolean localPass = true;
        for (int i = 0; i < inputCount; i++) {
            String inputName = "input" + i;
            boolean inputPassed = getBool(getValue(inputName));
            localPass = localPass && inputPassed;
        }
        if (permanentFailureCondition) {
            testPassed = testPassed && localPass;
        } else {
            testPassed = localPass;
        }

        for (int i = 0; i < inputCount; i++) {
            String result = (String) getValue("message" + i);
            if (result != null) {
                messages.append(result).append(System.lineSeparator());
            }
        }
        logMeasures(time);
        testPassed = testPassed && evaluateMeasures(time);
        synchronizeFieldsToMap();
    }

    private boolean evaluateMeasures(long time) {
        boolean allInRange = true;

        if (perStepMeasuresTarget.containsKey(time)) {
            //only check measures if they should be set at this step
            for (int i = 0; i < measuresCount; i++) {
                String key = measuresIdMapping.get(i).getMeasureInputName();
                String measureInputString = (String) getValue(key);
                assert measuresIdMapping.containsKey(i);
                MeasureAttribute currentAttribute = measuresIdMapping.get(i);

                Measure currentMeasure;
                try {
                    currentMeasure = measureConvertor.getMeasure(measureInputString);
                } catch (IOException e) {
                    logger.warn("Bad measure received", e);
                    continue;
                }
                receivedMeasuresCurrentStep.put(i, currentMeasure);
                ExpectedResult expectedResult = perStepMeasuresTarget.get(time).getMeasureAttributes().get(currentAttribute);

                if (measureInputString == null || expectedResult == null) {
                    continue;
                }
                //compare to expected value
                allInRange = allInRange && expectedResult.isInRange(currentMeasure);
            }
        }
        return allInRange;
    }

    /**
     * Logs all received measures
     *
     * @param time current simulation time step
     */
    private void logMeasures(long time) {
        JsonTestStep jsonTestStep = collectedMeasures.get(time);
        if (jsonTestStep == null) {
            jsonTestStep = new JsonTestStep();
            collectedMeasures.put(time, jsonTestStep);
        }

        for (int i = 0; i < measuresCount; i++) {
            String key = measuresIdMapping.get(i).getMeasureInputName();
            String measureInputString;
            measureInputString = (getValue(key)).toString();

            assert measuresIdMapping.containsKey(i);
            Measure currentMeasure;
            try {
                currentMeasure = measureConvertor.getMeasure(measureInputString);
            } catch (IOException e) {
                logger.warn("Bad measure: " + key, e);
                continue;
            }
            receivedMeasuresCurrentStep.put(i, currentMeasure);
            if (currentMeasure == null) {
                continue;
            }
            jsonTestStep.addObservedMeasure(currentMeasure);
        }
    }


    @Override
    public void synchronizeFieldsToMap() {
        setValue(TEST_RESULT_VAR_NAME, testPassed);
        setValue("MESSAGES", messages.toString()); //1 way sync only
        setValue("receivedMeasures", receivedMeasuresCurrentStep);
        setValue("allMeasuresProgress", collectedMeasures);
    }

    @Override
    public void synchronizeMapToFields() {
        testPassed = getBool(getValue(TEST_RESULT_VAR_NAME));
    }

    @Override
    public Map<String, String> lastSimulationStep() {
        evaluateMeasures(FINAL_STEP_TIME);
        synchronizeFieldsToMap();

        String finalResultsJson = null;
        try {
            finalResultsJson = measureConvertor.getFinalResultsJson(receivedMeasuresCurrentStep);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }

        Map<String, String> results = new HashMap<>();
        results.put("measures", finalResultsJson);
        String allFinalResultsJson = null;
        try {
            allFinalResultsJson = measureConvertor.getAllFinalResultsJson(collectedMeasures);
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        results.put("allMeasures", allFinalResultsJson);

        return results;
    }
}
