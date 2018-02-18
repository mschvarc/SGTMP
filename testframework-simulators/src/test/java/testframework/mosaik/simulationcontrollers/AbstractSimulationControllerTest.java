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

import org.junit.Test;
import org.mockito.Mockito;
import testframework.mosaik.models.AbstractModel;
import testframework.mosaik.models.Model;
import testframework.mosaik.models.ModelTestEvaluator;
import testframework.mosaik.models.factory.ModelFactory;
import testframework.mosaik.models.factory.TestEvaluatorFactory;

import java.util.HashMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static testframework.mosaik.simulationcontrollers.AbstractSimulationController.ATTACHED_TEST_EVALUATOR;
import static testframework.mosaik.simulationcontrollers.AbstractSimulationController.STANDALONE_TEST_EVALUATOR;

public class AbstractSimulationControllerTest {

    @Test
    public void testEvaluatorsDefault() {
        HashMap<String, Object> map = new HashMap<>();
        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);

        SimulationController controller = new SimulationController(modelFactory);
        assertThat(controller.isStandaloneTestEvaluator("Model", map)).isFalse();
        assertThat(controller.requiresAttachedTestEvaluator("Model", map)).isFalse();
    }

    @Test
    public void testStandaloneEvaluatorTrue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(STANDALONE_TEST_EVALUATOR, true);
        map.put(ATTACHED_TEST_EVALUATOR, false);

        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);

        SimulationController controller = new SimulationController(modelFactory);
        assertThat(controller.isStandaloneTestEvaluator("Model1", map)).isTrue();
        assertThat(controller.requiresAttachedTestEvaluator("Model1", map)).isFalse();
    }

    @Test
    public void testAttachedEvaluatorTrue() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(STANDALONE_TEST_EVALUATOR, false);
        map.put(ATTACHED_TEST_EVALUATOR, true);

        ModelTestEvaluator modelEval = Mockito.mock(ModelTestEvaluator.class);
        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        TestEvaluatorFactory testEvaluatorFactory = Mockito.mock(TestEvaluatorFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);
        when(testEvaluatorFactory.createNewAttachedTestEvaluator(model, map)).thenReturn(modelEval);

        SimulationController controller = new SimulationController(modelFactory);

        assertThat(controller.isStandaloneTestEvaluator("Model", map)).isFalse();
        assertThat(controller.requiresAttachedTestEvaluator("Model", map)).isTrue();
    }

    @Test
    public void testAddModelFactory() {
        HashMap<String, Object> map = new HashMap<>();

        ModelTestEvaluator modelEval = Mockito.mock(ModelTestEvaluator.class);
        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        TestEvaluatorFactory testEvaluatorFactory = Mockito.mock(TestEvaluatorFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);
        when(testEvaluatorFactory.createNewAttachedTestEvaluator(model, map)).thenReturn(modelEval);

        SimulationController controller = new SimulationController(modelFactory);
        controller.addModel("Model", map);
        assertThat(controller.getModels().size()).isEqualTo(1);
        assertThat(controller.getSimulationModels().size()).isEqualTo(1);
        assertThat(controller.getTestEvaluators()).isEmpty();
    }

    @Test
    public void testAddModelAndSimulatorFactory() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(STANDALONE_TEST_EVALUATOR, false);
        map.put(ATTACHED_TEST_EVALUATOR, true);

        ModelTestEvaluator modelEval = Mockito.mock(ModelTestEvaluator.class);
        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        TestEvaluatorFactory testEvaluatorFactory = Mockito.mock(TestEvaluatorFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);
        when(testEvaluatorFactory.createNewAttachedTestEvaluator(model, map)).thenReturn(modelEval);

        SimulationController controller = new SimulationController(modelFactory, testEvaluatorFactory);

        controller.addModel("Model", map);
        assertThat(controller.getModels().size()).isEqualTo(1);
        assertThat(controller.getSimulationModels().size()).isEqualTo(1);
        assertThat(controller.getTestEvaluators().size()).isEqualTo(1);
    }

    @Test
    public void testAddModelGetSet() {
        String key = "key";
        String value = "value";
        HashMap<String, Object> map = new HashMap<>();

        Model model = mock(Model.class);
        ModelFactory modelFactory = Mockito.mock(ModelFactory.class);
        when(modelFactory.createNewModel(map)).thenReturn(model);
        when(model.getValue(key)).thenReturn(value);

        SimulationController controller = new SimulationController(modelFactory);
        controller.addModel("Model", map);
        controller.setValue(0, key, value);
        Object result = controller.getValue(0, key);

        verify(model, times(1)).setValue(key, value);
        verify(model, times(1)).getValue(key);
        assertThat(result).isEqualTo(value);
    }

    @Test
    public void testAddModelStep() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(STANDALONE_TEST_EVALUATOR, false);
        map.put("init_val", 0);
        AbstractModel model = Mockito.mock(AbstractModel.class);
        ModelFactory factory = Mockito.mock(ModelFactory.class);
        when(factory.createNewModel(map)).thenReturn(model);
        SimulationController controller = new SimulationController(factory);
        controller.addModel("Model", map);
        controller.step(1);
        verify(model, Mockito.times(0)).step(0);
        verify(model, Mockito.times(1)).step(1);
    }

    private class SimulationController extends AbstractSimulationController {
        public SimulationController(ModelFactory modelFactory) {
            super(modelFactory);
        }

        public SimulationController(ModelFactory modelFactory, TestEvaluatorFactory testEvaluatorFactory) {
            super(modelFactory, testEvaluatorFactory);
        }

        public SimulationController(TestEvaluatorFactory testEvaluatorFactory) {
            super(testEvaluatorFactory);
        }
    }

}
