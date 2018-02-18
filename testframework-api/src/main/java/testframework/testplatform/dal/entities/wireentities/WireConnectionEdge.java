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

package testframework.testplatform.dal.entities.wireentities;

import testframework.testplatform.dal.entities.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class WireConnectionEdge extends BaseEntity {
    private String inputName;
    private String outputName;

    @ManyToOne
    private WireModel source;
    @ManyToOne
    private WireModel destination;

    private boolean isDirect;
    private boolean asyncRequests;

    private String syncTargetName = null;

    public WireConnectionEdge() {
    }

    public WireConnectionEdge(String inputName, String outputName, WireModel source, WireModel destination, boolean isDirect, boolean asyncRequests) {
        this.inputName = inputName;
        this.outputName = outputName;
        this.source = source;
        this.destination = destination;
        this.isDirect = isDirect;
        this.asyncRequests = asyncRequests;
    }

    public String getSyncTargetName() {
        return syncTargetName;
    }

    public void setSyncTargetName(String syncTargetName) {
        this.syncTargetName = syncTargetName;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public WireModel getSource() {
        return source;
    }

    public void setSource(WireModel source) {
        this.source = source;
    }

    public WireModel getDestination() {
        return destination;
    }

    public void setDestination(WireModel destination) {
        this.destination = destination;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public void setDirect(boolean direct) {
        isDirect = direct;
    }

    public boolean isAsyncRequests() {
        return asyncRequests;
    }

    public void setAsyncRequests(boolean asyncRequests) {
        this.asyncRequests = asyncRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireConnectionEdge)) return false;
        if (!super.equals(o)) return false;

        WireConnectionEdge that = (WireConnectionEdge) o;

        if (isDirect() != that.isDirect()) return false;
        if (isAsyncRequests() != that.isAsyncRequests()) return false;
        if (getInputName() != null ? !getInputName().equals(that.getInputName()) : that.getInputName() != null)
            return false;
        if (getOutputName() != null ? !getOutputName().equals(that.getOutputName()) : that.getOutputName() != null)
            return false;
        if (getSource() != null ? !getSource().equals(that.getSource()) : that.getSource() != null) return false;
        if (getDestination() != null ? !getDestination().equals(that.getDestination()) : that.getDestination() != null)
            return false;
        return getSyncTargetName() != null ? getSyncTargetName().equals(that.getSyncTargetName()) : that.getSyncTargetName() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getInputName() != null ? getInputName().hashCode() : 0);
        result = 31 * result + (getOutputName() != null ? getOutputName().hashCode() : 0);
        result = 31 * result + (getSource() != null ? getSource().hashCode() : 0);
        result = 31 * result + (getDestination() != null ? getDestination().hashCode() : 0);
        result = 31 * result + (isDirect() ? 1 : 0);
        result = 31 * result + (isAsyncRequests() ? 1 : 0);
        result = 31 * result + (getSyncTargetName() != null ? getSyncTargetName().hashCode() : 0);
        return result;
    }
}
