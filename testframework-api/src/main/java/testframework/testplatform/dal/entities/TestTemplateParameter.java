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

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TestTemplateParameter extends BaseEntity {

    @NotNull
    @Length(min = 1)
    private String key;

    private Long minimumValue;

    private Long maximumValue;

    @NotNull
    @Enumerated
    private ReplaceMode replaceMode;

    @ElementCollection
    private List<String> literals = new ArrayList<>();

    public List<String> getLiterals() {
        return literals;
    }

    public void setLiterals(List<String> literals) {
        this.literals.clear();
        this.literals.addAll(literals);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(Long minimumValue) {
        this.minimumValue = minimumValue;
    }

    public Long getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(Long maximumValue) {
        this.maximumValue = maximumValue;
    }

    public ReplaceMode getReplaceMode() {
        return replaceMode;
    }

    public void setReplaceMode(ReplaceMode replaceMode) {
        this.replaceMode = replaceMode;
    }

    public List<TestTemplateValue> allValues() {
        ArrayList<TestTemplateValue> values = new ArrayList<>();
        switch (replaceMode) {
            case TEST_BOUNDARY_INCLUSIVE:
                assert minimumValue != null && maximumValue != null;
                values.add(new TestTemplateValue(this, key, minimumValue));
                values.add(new TestTemplateValue(this, key, maximumValue));
                break;
            case TEST_BOUNDARY_AND_MID:
                assert minimumValue != null && maximumValue != null;
                values.add(new TestTemplateValue(this, key, minimumValue));
                values.add(new TestTemplateValue(this, key, (maximumValue + minimumValue) / 2));
                values.add(new TestTemplateValue(this, key, maximumValue));
                break;
            case STRING_LITERALS:
                for (String literal : literals) {
                    values.add(new TestTemplateValue(this, key, literal));
                }
                break;
            default:
                throw new IllegalStateException("Unhandled enum case");

        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTemplateParameter)) return false;
        if (!super.equals(o)) return false;

        TestTemplateParameter that = (TestTemplateParameter) o;

        if (getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null) return false;
        return getReplaceMode() == that.getReplaceMode();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
        result = 31 * result + (getReplaceMode() != null ? getReplaceMode().hashCode() : 0);
        return result;
    }

    public enum ReplaceMode {

        /*
         * Tests single value selected randomly from the inclusive range from min to max
        TEST_RANDOM,
        */

        /**
         * Tests min and max boundary values
         */
        TEST_BOUNDARY_INCLUSIVE,

        /**
         * Tests min, max and middle values
         */
        TEST_BOUNDARY_AND_MID,

        /**
         * Returns string literals
         */
        STRING_LITERALS
    }
}
