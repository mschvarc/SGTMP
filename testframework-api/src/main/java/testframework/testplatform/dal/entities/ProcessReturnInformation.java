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

package testframework.testplatform.dal.entities;


import java.util.List;

public class ProcessReturnInformation {

    private List<String> processArgs;
    private int exitCode;
    private String stdout;
    private String errout;

    public List<String> getProcessArgs() {
        return processArgs;
    }

    public void setProcessArgs(List<String> processArgs) {
        this.processArgs = processArgs;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getErrout() {
        return errout;
    }

    public void setErrout(String errout) {
        this.errout = errout;
    }
}
