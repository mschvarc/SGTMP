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

package testframework.testplatform;

/**
 * Constants used in the Testing Management Platform
 */
public final class Constants {

    public static final String SIMULATION_CONFIG_XML = "simulation_config.xml";
    public static final String SIMULATORS_XML = "simulators.xml";
    public static final String MODELS_XML = "models.xml";
    public static final String WIRES_XML = "wires.xml";
    public static final String CONFIG_XML = "config.xml";
    public static final String TESTRESULT_XML = "testresult.xml";
    public static final String TEST_EVAL_INJECTED_PATH = "testEvalInjectedPath";
    public static final String GLOBAL_TEST_EVAL_PERMANENT_FAILURE_ENTRY = "globalTestEvalPermanentFailureEntry";
    public static final String TEST_RESULT_NAME = "testResultName";
    public static final String TESTLAUNCHER_GLOBAL_CONFIG_PATH = "${testlauncher.globalConfigPath}";
    public static final String TESTLAUNCHER_TESTS_DIRECTORY = "${testlauncher.testsDirectory}";
    public static final String TESTLAUNCHER_PYTHON_PLATFORM_FILE = "${testlauncher_pythonPlatformFile}";
    public static final String TESTLAUNCHER_MOSAIK_ENVIRONMENT_DIR = "${testlauncher_mosaikEnvDir}";

    private Constants() {
        //NO-OP
    }
}
