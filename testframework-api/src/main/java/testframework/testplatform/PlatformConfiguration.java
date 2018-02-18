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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static testframework.testplatform.Constants.TESTLAUNCHER_MOSAIK_ENVIRONMENT_DIR;
import static testframework.testplatform.Constants.TESTLAUNCHER_PYTHON_PLATFORM_FILE;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class PlatformConfiguration {

    public static final long PROCESS_QUEUE_SHUTDOWN_TIMEOUT = 2;
    public static final TimeUnit PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final String globalConfigPath;
    private final String testsDirectory;
    private Path mosaikEnv;
    private Path pythonPlatform;
    private boolean mosaikRedirectErrorStream;
    private boolean printResults;


    @Autowired
    public PlatformConfiguration(
            @Value(value = "${testlauncher_globalConfigPath:src/test/resources/tests/global_config.xml}") String globalConfigPath,
            @Value(value = "${testlauncher_testsDirectory:src/test/resources/tests}") String testsDirectory,
            @Value(value = TESTLAUNCHER_MOSAIK_ENVIRONMENT_DIR) String mosaikEnvDirectory,
            @Value(value = TESTLAUNCHER_PYTHON_PLATFORM_FILE) String pythonPlatformFile,
            @Value(value = "${testlauncher_mosaikRedirectErrorStream:true}") String mosaikRedirectErrorStream,
            @Value(value = "${testlauncher_printResults:true}") String printResults) {
        this.globalConfigPath = globalConfigPath;
        this.testsDirectory = testsDirectory;
        this.mosaikEnv = Paths.get(mosaikEnvDirectory);
        this.pythonPlatform = Paths.get(pythonPlatformFile);
        this.mosaikRedirectErrorStream = Boolean.parseBoolean(mosaikRedirectErrorStream);
        this.printResults = Boolean.parseBoolean(printResults);
    }

    public String getGlobalConfigPath() {
        return globalConfigPath;
    }

    public String getTestsDirectory() {
        return testsDirectory;
    }

    public Path getMosaikEnv() {
        return mosaikEnv;
    }

    public Path getPythonPlatform() {
        return pythonPlatform;
    }

    public boolean isMosaikRedirectErrorStream() {
        return mosaikRedirectErrorStream;
    }

    public boolean isPrintResults() {
        return printResults;
    }
}
