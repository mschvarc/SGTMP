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

package testframework.testplatform.dal.entities.measure;

import testframework.testplatform.dal.entities.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class JsonTestStep extends BaseEntity {

    @ManyToMany
    private List<Measure> observedMeasures = new ArrayList<>();

    public List<Measure> getObservedMeasures() {
        return observedMeasures;
    }

    public void setObservedMeasures(List<Measure> observedMeasures) {
        this.observedMeasures = observedMeasures;
    }

    public void addObservedMeasure(Measure measure) {
        getObservedMeasures().add(measure);
    }

    @Override
    public String toString() {
        return "JsonTestStep{" +
                "observedMeasures=" + observedMeasures +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonTestStep)) return false;
        if (!super.equals(o)) return false;

        JsonTestStep that = (JsonTestStep) o;

        return getObservedMeasures() != null ? getObservedMeasures().equals(that.getObservedMeasures()) : that.getObservedMeasures() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getObservedMeasures() != null ? getObservedMeasures().hashCode() : 0);
        return result;
    }
}
