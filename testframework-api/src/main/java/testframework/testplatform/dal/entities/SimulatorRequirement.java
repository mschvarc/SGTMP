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

import testframework.testplatform.dal.entities.wireentities.WireSimulator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SimulatorRequirement extends Requirement {

    @ManyToMany(cascade = CascadeType.ALL)
    private List<WireSimulator> simulators = new ArrayList<>();

    public List<WireSimulator> getSimulators() {
        return simulators;
    }

    public void setSimulators(List<WireSimulator> simulators) {
        this.simulators = simulators;
    }

    @Override
    public boolean contains(Requirement other) {
        if (!(other instanceof SimulatorRequirement)) {
            return false;
        }
        SimulatorRequirement otherReq = (SimulatorRequirement) other;
        return this.simulators.containsAll(otherReq.simulators);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimulatorRequirement)) return false;
        if (!super.equals(o)) return false;

        SimulatorRequirement that = (SimulatorRequirement) o;

        return getSimulators() != null ? getSimulators().equals(that.getSimulators()) : that.getSimulators() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getSimulators() != null ? getSimulators().hashCode() : 0);
        return result;
    }
}
