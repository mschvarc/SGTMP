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

package testframework.configurationgeneration;

import org.junit.Before;
import org.junit.Test;
import testframework.testplatform.configurationgenerator.TemplateReplacer;
import testframework.testplatform.configurationgenerator.TemplateReplacerImpl;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static testframework.testplatform.configurationgenerator.TemplateReplacer.LEFT_ESCAPE_SYMBOL;
import static testframework.testplatform.configurationgenerator.TemplateReplacer.RIGHT_ESCAPE_SYMBOL;
import static testframework.testplatform.configurationgenerator.TemplateReplacer.VARIABLE_ESCAPE_SYMBOL;

public class TemplateReplacerTest {

    private TemplateReplacer templateReplacer;

    @Before
    public void setup() {
        templateReplacer = new TemplateReplacerImpl();
    }

    @Test
    public void testNoReplacement() {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
        }};
        final String input = "123abcd";

        String result = templateReplacer.replace(input, mapping);
        assertThat(result).isEqualTo("123abcd");
    }

    @Test
    public void testStringReplacement() {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
        }};
        final String input = VARIABLE_ESCAPE_SYMBOL + LEFT_ESCAPE_SYMBOL + "key1" + RIGHT_ESCAPE_SYMBOL;

        String result = templateReplacer.replace(input, mapping);
        final String expected = "value1";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testDoubleReplacement() {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
            put("key2", "value2");
        }};
        final String input = String.format("%s%s%s%s <=> %s%s%s%s", VARIABLE_ESCAPE_SYMBOL, LEFT_ESCAPE_SYMBOL, "key1", RIGHT_ESCAPE_SYMBOL,
                VARIABLE_ESCAPE_SYMBOL, LEFT_ESCAPE_SYMBOL, "key2", RIGHT_ESCAPE_SYMBOL);

        String result = templateReplacer.replace(input, mapping);
        final String expected = "value1 <=> value2";
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testNoReplacementWithoutEscape() {
        HashMap<String, String> mapping = new HashMap<String, String>() {{
            put("key1", "value1");
        }};
        final String input = LEFT_ESCAPE_SYMBOL + "key1" + RIGHT_ESCAPE_SYMBOL;

        String result = templateReplacer.replace(input, mapping);
        assertThat(result).isEqualTo(input);
    }

}
