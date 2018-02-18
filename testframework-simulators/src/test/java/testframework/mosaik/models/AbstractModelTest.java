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

import com.google.gson.Gson;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractModelTest {

    private Gson gson = new Gson();

    @Test
    public void testKeyValueString() {
        AbstractModel model = new AbstractModelImpl();
        String key = "key";
        String value = "value";
        model.setValue(key, value);
        assertThat(model.getValue(key)).isEqualTo(value);
        assertThat(model.getValue(key)).isInstanceOf(value.getClass());
    }

    @Test
    public void testKeyValueJson() {
        AbstractModel model = new AbstractModelImpl();
        List<String> original = Collections.unmodifiableList(Arrays.asList("item1", "item2"));
        String key = "key";
        String value = gson.toJson(original);
        model.setValue(key, value);
        assertThat(model.getValue(key)).isEqualTo(value);
        assertThat(model.getValue(key)).isInstanceOf(value.getClass());
    }

    @Test
    public void testKeyValueNull() {
        final AbstractModel model = new AbstractModelImpl();
        final String key = "key";
        final Object value = null;
        model.setValue(key, value);
        assertThat(model.getValue(key)).isEqualTo(value);
        assertThat(model.getValue(key)).isNull();
    }

    private class AbstractModelImpl extends AbstractModel {
        @Override
        public void step(long time) {
            throw new AssertionError();
        }

        @Override
        public void synchronizeFieldsToMap() {
            //NO-OP
        }

        @Override
        public void synchronizeMapToFields() {
            //NO-OP
        }
    }

}
