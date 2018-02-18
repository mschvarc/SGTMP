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
public class WireModel extends BaseEntity {

    @ManyToOne
    private WireSimulator simulator;
    private String modelId;
    private int modelIndex;
    private boolean requiresAttachedTestEvaluator;
    private boolean isStandaloneTestEvaluator;

    public WireModel() {
    }

    public WireModel(WireSimulator simulator, String modelId, int modelIndex) {
        this.simulator = simulator;
        this.modelId = modelId;
        this.modelIndex = modelIndex;
    }

    public WireSimulator getSimulator() {
        return simulator;
    }

    public void setSimulator(WireSimulator simulator) {
        this.simulator = simulator;
    }

    public int getModelIndex() {
        return modelIndex;
    }

    public void setModelIndex(int modelIndex) {
        this.modelIndex = modelIndex;
    }

    public boolean isRequiresAttachedTestEvaluator() {
        return requiresAttachedTestEvaluator;
    }

    public void setRequiresAttachedTestEvaluator(boolean requiresAttachedTestEvaluator) {
        this.requiresAttachedTestEvaluator = requiresAttachedTestEvaluator;
    }

    public boolean isStandaloneTestEvaluator() {
        return isStandaloneTestEvaluator;
    }

    public void setStandaloneTestEvaluator(boolean standaloneTestEvaluator) {
        isStandaloneTestEvaluator = standaloneTestEvaluator;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    @Override
    public String toString() {
        return "WireModel{" +
                "simulator=" + simulator +
                ", modelId='" + modelId + '\'' +
                ", modelIndex=" + modelIndex +
                ", requiresAttachedTestEvaluator=" + requiresAttachedTestEvaluator +
                ", isStandaloneTestEvaluator=" + isStandaloneTestEvaluator +
                '}';
    }

    public String getFullName() {
        return simulator.getSimulatorId() + "_at_" + modelId + "_idx_" + modelIndex + "_";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WireModel)) return false;
        if (!super.equals(o)) return false;

        WireModel wireModel = (WireModel) o;

        if (getModelIndex() != wireModel.getModelIndex()) return false;
        if (isRequiresAttachedTestEvaluator() != wireModel.isRequiresAttachedTestEvaluator()) return false;
        if (isStandaloneTestEvaluator() != wireModel.isStandaloneTestEvaluator()) return false;
        if (getSimulator() != null ? !getSimulator().equals(wireModel.getSimulator()) : wireModel.getSimulator() != null)
            return false;
        return getModelId() != null ? getModelId().equals(wireModel.getModelId()) : wireModel.getModelId() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getSimulator() != null ? getSimulator().hashCode() : 0);
        result = 31 * result + (getModelId() != null ? getModelId().hashCode() : 0);
        result = 31 * result + getModelIndex();
        result = 31 * result + (isRequiresAttachedTestEvaluator() ? 1 : 0);
        result = 31 * result + (isStandaloneTestEvaluator() ? 1 : 0);
        return result;
    }
}
