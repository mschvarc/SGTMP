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

package testframework.mosaik.util;

/**
 * Holds data for async communications
 */
public class AsyncDataHolder {

    private final String outputName;
    private final String destinationInputName;
    private final String synchroniseName;

    public AsyncDataHolder(String outputName, String destinationInputName, String synchroniseName) {
        this.outputName = outputName;
        this.destinationInputName = destinationInputName;
        this.synchroniseName = synchroniseName;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getDestinationInputName() {
        return destinationInputName;
    }

    public String getSynchroniseName() {
        return synchroniseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsyncDataHolder)) return false;

        AsyncDataHolder that = (AsyncDataHolder) o;

        if (getOutputName() != null ? !getOutputName().equals(that.getOutputName()) : that.getOutputName() != null)
            return false;
        if (getDestinationInputName() != null ? !getDestinationInputName().equals(that.getDestinationInputName()) : that.getDestinationInputName() != null)
            return false;
        return getSynchroniseName() != null ? getSynchroniseName().equals(that.getSynchroniseName()) : that.getSynchroniseName() == null;
    }

    @Override
    public int hashCode() {
        int result = getOutputName() != null ? getOutputName().hashCode() : 0;
        result = 31 * result + (getDestinationInputName() != null ? getDestinationInputName().hashCode() : 0);
        result = 31 * result + (getSynchroniseName() != null ? getSynchroniseName().hashCode() : 0);
        return result;
    }
}
