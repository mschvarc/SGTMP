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

import testframework.testplatform.dal.entities.ProcessReturnInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Wraps a process launched by the Testing Platform
 */
public final class ProcessInfoWrapper {

    private final List<ProcessHandler> processHandlers;
    private final List<Future<ProcessReturnInformation>> futureReturns;
    private final List<ProcessReturnInformation> returnInformations = new ArrayList<>();

    public ProcessInfoWrapper(List<ProcessHandler> processHandlers, List<Future<ProcessReturnInformation>> futureReturns) {
        this.processHandlers = processHandlers;
        this.futureReturns = futureReturns;
    }

    public List<ProcessHandler> getProcessHandlers() {
        return processHandlers;
    }

    public List<Future<ProcessReturnInformation>> getFutureReturns() {
        return futureReturns;
    }

    public List<ProcessReturnInformation> getReturnInformations() {
        return returnInformations;
    }

    public void addReturnInformation(ProcessReturnInformation info) {
        this.returnInformations.add(info);
    }
}
