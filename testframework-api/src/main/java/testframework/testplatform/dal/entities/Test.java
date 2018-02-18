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

import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Test extends BaseEntity {

    @Column(updatable = false, nullable = false, unique = true)
    private String uuid;

    @NotNull
    @Length(min = 1)
    private String testName;

    private String testDescription;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TestTemplateParameter> templateValues = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Requirement> testRequirements = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestStep> testSteps = new ArrayList<>();
    /**
     * Designates if deviation from test pass for even one step should result in permanent test failure
     */
    private boolean oneStepPermanentFailure;

    @ManyToMany
    public List<Requirement> getTestRequirements() {
        return testRequirements;
    }

    public void setTestRequirements(List<Requirement> testRequirements) {
        this.testRequirements = testRequirements;
    }

    @PrePersist
    public void initializeUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public String getUuid() {
        if (uuid == null) {
            initializeUUID();
        }
        return uuid;
    }

    public void setUuid(String id) {
        this.uuid = id;
    }

    public boolean isOneStepPermanentFailure() {
        return oneStepPermanentFailure;
    }

    public void setOneStepPermanentFailure(boolean oneStepPermanentFailure) {
        this.oneStepPermanentFailure = oneStepPermanentFailure;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public List<TestTemplateParameter> getTemplateValues() {
        return templateValues;
    }

    public void setTemplateValues(List<TestTemplateParameter> templateValues) {
        this.templateValues = templateValues;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = new ArrayList<>(testSteps);
    }

    @Override
    public String toString() {
        return "Test{" +
                "uuid=" + uuid +
                ", testName='" + testName + '\'' +
                ", testDescription='" + testDescription + '\'' +
                ", templateValues=" + templateValues +
                ", testRequirements=" + testRequirements +
                ", oneStepPermanentFailure=" + oneStepPermanentFailure +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Test)) return false;
        if (!super.equals(o)) return false;

        Test test = (Test) o;

        if (getUuid() != null ? !getUuid().equals(test.getUuid()) : test.getUuid() != null) return false;
        return getTestName() != null ? getTestName().equals(test.getTestName()) : test.getTestName() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        result = 31 * result + (getTestName() != null ? getTestName().hashCode() : 0);
        return result;
    }

    public List<SimulatorRequirement> getSimulatorRequirements() {
        return testRequirements.stream()
                .filter(req -> (req instanceof SimulatorRequirement))
                .map(req -> (SimulatorRequirement) req)
                .collect(Collectors.toList());
    }
}
