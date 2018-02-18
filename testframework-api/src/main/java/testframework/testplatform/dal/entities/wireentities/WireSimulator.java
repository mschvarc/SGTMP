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

@Entity
public class WireSimulator extends BaseEntity {
    private String simulatorId;
    private boolean virtual = false;

    public WireSimulator() {
    }

    public WireSimulator(String simulatorId) {
        this.simulatorId = simulatorId;
    }

    public WireSimulator(String simulatorId, boolean virtual) {
        this.simulatorId = simulatorId;
        this.virtual = virtual;
    }

    public String getSimulatorId() {
        return simulatorId;
    }

    public void setSimulatorId(String simulatorId) {
        this.simulatorId = simulatorId;
    }

    public boolean isVirtual() {
        return virtual;
    }

    @Override
    public String toString() {
        return "WireSimulator{" +
                "simulatorId='" + simulatorId + '\'' +
                ", virtual=" + virtual +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireSimulator)) return false;
        if (!super.equals(o)) return false;

        WireSimulator that = (WireSimulator) o;

        if (virtual != that.virtual) return false;
        return getSimulatorId() != null ? getSimulatorId().equals(that.getSimulatorId()) : that.getSimulatorId() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getSimulatorId() != null ? getSimulatorId().hashCode() : 0);
        result = 31 * result + (virtual ? 1 : 0);
        return result;
    }
}
