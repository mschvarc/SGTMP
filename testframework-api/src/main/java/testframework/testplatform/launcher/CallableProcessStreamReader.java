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

package testframework.testplatform.launcher;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import static testframework.testplatform.PlatformConfiguration.PROCESS_QUEUE_SHUTDOWN_TIMEOUT;
import static testframework.testplatform.PlatformConfiguration.PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT;

/**
 * Class for reading the output (stdout and stderr) of a process
 *
 * @param <ReturnType>      String based return type of the process
 * @param <ProcessType>     Process
 * @param <InputStreamType> Process InputStream
 */
final class CallableProcessStreamReader<ReturnType extends java.lang.String,
        ProcessType extends Process,
        InputStreamType extends InputStream>
        implements Callable<ReturnType> {

    private static final Logger logger = Logger.getLogger(CallableProcessStreamReader.class);
    private final ProcessType process;
    private final InputStreamType stream;
    private final StringBuilder processOutput = new StringBuilder();


    CallableProcessStreamReader(ProcessType process, InputStreamType stream) {
        this.process = process;
        this.stream = stream;
    }

    @Override
    public ReturnType call() throws Exception {
        try (BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(stream))) {
            String readLine;
            while ((readLine = processOutputReader.readLine()) != null) {
                processOutput.append(readLine).append(System.lineSeparator());
            }
            try {
                process.waitFor(PROCESS_QUEUE_SHUTDOWN_TIMEOUT, PROCESS_QUEUE_SHUTDOWN_TIMEOUT_UNIT);
            } catch (InterruptedException ex) {
                logger.warn(ex);
                Thread.currentThread().interrupt();
            }
        }
        @SuppressWarnings("unchecked")
        ReturnType result = (ReturnType) processOutput.toString();
        return result;
    }
}
