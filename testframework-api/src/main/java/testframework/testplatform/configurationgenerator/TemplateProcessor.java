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

import java.nio.file.Path;
import java.util.Map;

/**
 * Replaces templates in a file
 */
public interface TemplateProcessor {

    /**
     * Replaces template values with their actual values
     *
     * @param input  source files
     * @param output output files
     * @param tokens key-values used for replacement
     */
    void replaceTemplates(Path[] input, Path[] output, Map<String, String> tokens);

    /**
     * Replaces template values with their actual values
     *
     * @param input  source file
     * @param output output file
     * @param tokens key-values used for replacement
     */
    void replaceTemplate(Path input, Path output, Map<String, String> tokens);

}
