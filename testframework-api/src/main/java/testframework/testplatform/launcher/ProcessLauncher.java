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
import org.springframework.stereotype.Component;
import testframework.testplatform.exceptions.TestRunException;
import testframework.testplatform.dal.entities.ProcessReturnInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public final class ProcessLauncher {

    private static final Logger logger = Logger.getLogger(ProcessLauncher.class);

    public ProcessInfoWrapper startProcessesNonBlocking(List<List<String>> programs, boolean redirectErrorStream) {
        return launchAllNonBlocking(programs, redirectErrorStream);
    }


    private ProcessInfoWrapper launchAllNonBlocking(List<List<String>> programs, boolean redirectErrorStream) {

        final List<ProcessHandler> processHandlers = new ArrayList<>();
        final List<Future<ProcessReturnInformation>> futureReturns = new ArrayList<>();

        for (List<String> program : programs) {
            ProcessHandler newProcess = new ProcessHandler(program.toArray(new String[program.size()]), redirectErrorStream);
            processHandlers.add(newProcess);
            Future<ProcessReturnInformation> resultCode = newProcess.startNonBlocking();
            futureReturns.add(resultCode);
        }
        return new ProcessInfoWrapper(processHandlers, futureReturns);
    }

    public List<ProcessReturnInformation> getResultsBlocking(ProcessInfoWrapper wrapper, boolean printResults, long timeout, TimeUnit unit) throws TestRunException {
        List<Exception> thrownExceptions = new ArrayList<>();
        for (Future<ProcessReturnInformation> returnFuture : wrapper.getFutureReturns()) {
            try {
                ProcessReturnInformation returnInfo = returnFuture.get(timeout, unit);
                wrapper.addReturnInformation(returnInfo);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                //do not throw directly, need to iterate over ALL processes first!
                thrownExceptions.add(e);
            }
        }
        if (!thrownExceptions.isEmpty()) {
            throw new TestRunException(thrownExceptions);
        }
        return processResults(wrapper, printResults);
    }

    private List<ProcessReturnInformation> processResults(ProcessInfoWrapper wrapper, boolean printResults) {
        for (ProcessHandler processHandler : wrapper.getProcessHandlers()) {
            processHandler.awaitNonBlockingExit();
        }

        if (printResults) {
            for (ProcessReturnInformation information : wrapper.getReturnInformations()) {
                logger.info("");
                logger.info(information.getStdout());
                logger.info(information.getErrout());
                logger.info("Exit code: " + information.getExitCode() + " ----------");
            }
        }
        return wrapper.getReturnInformations();
    }

    public List<ProcessReturnInformation> getResultsBlocking(ProcessInfoWrapper wrapper, boolean printResults) throws TestRunException {
        for (Future<ProcessReturnInformation> returnFuture : wrapper.getFutureReturns()) {
            try {
                ProcessReturnInformation returnInfo = returnFuture.get();
                wrapper.addReturnInformation(returnInfo);
            } catch (ExecutionException | InterruptedException e) {
                throw new TestRunException(e);
            }
        }
        return processResults(wrapper, printResults);
    }

    public void forceCleanup(ProcessInfoWrapper wrapper) {
        for (ProcessHandler processHandler : wrapper.getProcessHandlers()) {
            processHandler.terminateForcefully();
        }
    }
}
