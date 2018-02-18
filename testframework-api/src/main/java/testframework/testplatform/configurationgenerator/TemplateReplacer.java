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

package testframework.testplatform.configurationgenerator;

import java.util.Map;

/**
 * Replaces template values in a XML file
 */
public interface TemplateReplacer {

    String VARIABLE_ESCAPE_SYMBOL = "$";
    String LEFT_ESCAPE_SYMBOL = "{";
    String RIGHT_ESCAPE_SYMBOL = "}";

    /**
     * Replaces all instances of userTemplateValues with their actual value in input file
     *
     * @param input              xml file content
     * @param userTemplateValues template key and values to replace
     * @return replaced XML file content
     */
    String replace(final String input, Map<String, String> userTemplateValues);
}
