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

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import testframework.testplatform.dal.entities.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class MeasureAttribute extends BaseEntity {

    @NotNull
    @Length(min = 1)
    private String name;
    @NotNull
    @Length(min = 1)
    private String simulatorId;
    @NotNull
    @Length(min = 1)
    private String modelId;
    @NotNull
    @Range(min = 0)
    private int modelIndex;
    @NotNull
    @Length(min = 1)
    private String measureInputName;

    public String getMeasureInputName() {
        return measureInputName;
    }

    public void setMeasureInputName(String measureInputName) {
        this.measureInputName = measureInputName;
    }

    public int getModelIndex() {
        return modelIndex;
    }

    public void setModelIndex(int modelIndex) {
        this.modelIndex = modelIndex;
    }

    public String getSimulatorId() {
        return simulatorId;
    }

    public void setSimulatorId(String simulatorId) {
        this.simulatorId = simulatorId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasureAttribute)) return false;
        if (!super.equals(o)) return false;

        MeasureAttribute that = (MeasureAttribute) o;

        if (getModelIndex() != that.getModelIndex()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getSimulatorId() != null ? !getSimulatorId().equals(that.getSimulatorId()) : that.getSimulatorId() != null)
            return false;
        if (getModelId() != null ? !getModelId().equals(that.getModelId()) : that.getModelId() != null) return false;
        return getMeasureInputName() != null ? getMeasureInputName().equals(that.getMeasureInputName()) : that.getMeasureInputName() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getSimulatorId() != null ? getSimulatorId().hashCode() : 0);
        result = 31 * result + (getModelId() != null ? getModelId().hashCode() : 0);
        result = 31 * result + getModelIndex();
        result = 31 * result + (getMeasureInputName() != null ? getMeasureInputName().hashCode() : 0);
        return result;
    }
}
