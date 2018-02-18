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

package testframework.mosaik.models.factory;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import testframework.mosaik.models.ModelGlobalTestEvaluator;
import testframework.mosaik.models.ModelTestEvaluator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalTestEvaluatorFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullStandalone() {
        final GlobalTestEvaluatorFactory factory = new GlobalTestEvaluatorFactory();
        factory.createNewStandaloneTestEvaluator(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAttached1() {
        final GlobalTestEvaluatorFactory factory = new GlobalTestEvaluatorFactory();
        factory.createNewAttachedTestEvaluator(null, new HashMap<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAttached2() {
        final GlobalTestEvaluatorFactory factory = new GlobalTestEvaluatorFactory();
        ModelGlobalTestEvaluator mock = Mockito.mock(ModelGlobalTestEvaluator.class);
        factory.createNewAttachedTestEvaluator(mock, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testAttachedValid() {
        final ModelGlobalTestEvaluator globalTestEvaluator = Mockito.mock(ModelGlobalTestEvaluator.class);
        final GlobalTestEvaluatorFactory factory = new GlobalTestEvaluatorFactory();
        factory.createNewAttachedTestEvaluator(globalTestEvaluator, new HashMap<>());

    }

    @Test
    public void testValidCreation() {
        final GlobalTestEvaluatorFactory factory = new GlobalTestEvaluatorFactory();

        Map<String, Object> map = new HashMap<>();
        map.put("__InputCount__", 0);
        map.put("__MeasuresCount__", 0);
        map.put("__PermanentFailureEntry__", false);
        map.put("__GlobalMeasureIdMapping__", getResourceContent("GlobalTestEvaluator_GlobalMeasureIdMapping.json"));
        map.put("__GlobalTargetMeasures__", getResourceContent("GlobalTestEvaluator_GlobalTargetMeasures.json"));

        ModelTestEvaluator evaluator = factory.createNewStandaloneTestEvaluator(map);
        assertThat(evaluator).isNotNull();
        assertThat(evaluator.getMessages()).isEmpty();
        assertThat(evaluator.isPassed()).isTrue();
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
