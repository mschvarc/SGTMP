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

package testframework.mosaik.simulationcontrollers;

import testframework.mosaik.models.Model;
import testframework.mosaik.models.ModelTestEvaluator;
import testframework.mosaik.models.factory.ModelFactory;
import testframework.mosaik.models.factory.TestEvaluatorFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages models and test evaluators for a specific simulator
 * All SGTMP aware simulation controllers must inherit from this class to use SGTMP functionality
 */
public abstract class AbstractSimulationController {
    public static final String ATTACHED_TEST_EVALUATOR = "__requiresAttachedTestEvaluator__";
    public static final String STANDALONE_TEST_EVALUATOR = "__StandaloneTestEvaluator__";

    private final ArrayList<Model> models = new ArrayList<>();
    private final ArrayList<ModelTestEvaluator> testEvaluators = new ArrayList<>();
    private final ArrayList<Model> simulationModels = new ArrayList<>();

    private final ModelFactory modelFactory;
    private final TestEvaluatorFactory testEvaluatorFactory;

    public AbstractSimulationController(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
        testEvaluatorFactory = null;
    }

    public AbstractSimulationController(ModelFactory modelFactory, TestEvaluatorFactory testEvaluatorFactory) {
        this.modelFactory = modelFactory;
        this.testEvaluatorFactory = testEvaluatorFactory;
    }

    public AbstractSimulationController(TestEvaluatorFactory testEvaluatorFactory) {
        this.testEvaluatorFactory = testEvaluatorFactory;
        this.modelFactory = null;
    }


    public void reset() {
        this.models.clear();
        this.testEvaluators.clear();
        this.simulationModels.clear();
    }


    public void addModel(String modelName, Map<String, Object> modelParams) {
        if (isStandaloneTestEvaluator(modelName, modelParams)) {
            assert testEvaluatorFactory != null;
            ModelTestEvaluator testEvaluatorModel = testEvaluatorFactory.createNewStandaloneTestEvaluator(modelParams);
            testEvaluators.add(testEvaluatorModel);
            models.add(testEvaluatorModel);
        } else {
            assert modelFactory != null;
            Model model = modelFactory.createNewModel(modelParams);
            models.add(model);
            simulationModels.add(model);

            //create attached test evaluator
            if (requiresAttachedTestEvaluator(modelName, modelParams)) {
                assert testEvaluatorFactory != null;
                ModelTestEvaluator attachedTestEvaluator = testEvaluatorFactory.createNewAttachedTestEvaluator(model, modelParams);
                testEvaluators.add(attachedTestEvaluator); //DO NOT add attachedTestEvaluator to models list, attachedTestEvaluator will write __TEST_PASS__ to model itself
            }
        }
    }

    /**
     * Called every simulations tep
     *
     * @param time
     */
    public void step(long time) {
        //first step through models
        for (Model model : simulationModels) {
            model.step(time);
        }
        //then evaluators
        for (ModelTestEvaluator model : testEvaluators) {
            model.step(time);
        }
    }

    public List<Model> getModels() {
        return Collections.unmodifiableList(this.models);
    }

    protected void cleanup() {
        this.models.clear();
        this.simulationModels.clear();
        this.testEvaluators.clear();
    }

    public List<Model> getSimulationModels() {
        return Collections.unmodifiableList(this.simulationModels);
    }

    public List<ModelTestEvaluator> getTestEvaluators() {
        return Collections.unmodifiableList(this.testEvaluators);
    }

    public Object getValue(int idx, String key) {
        Model model = this.models.get(idx);
        return model.getValue(key);
    }

    public void setValue(int idx, String key, Object value) {
        Model model = models.get(idx);
        model.setValue(key, value);
    }

    protected boolean requiresAttachedTestEvaluator(String modelName, Map<String, Object> modelParams) {
        return modelParams.containsKey(ATTACHED_TEST_EVALUATOR)
                && modelParams.get(ATTACHED_TEST_EVALUATOR).toString().equalsIgnoreCase("true");
    }

    protected boolean isStandaloneTestEvaluator(String modelName, Map<String, Object> modelParams) {
        return modelParams.containsKey(STANDALONE_TEST_EVALUATOR)
                && modelParams.get(STANDALONE_TEST_EVALUATOR).toString().equalsIgnoreCase("true");
    }

}
