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
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TestTemplateValue extends BaseEntity {

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private TestTemplateParameter templateParameter;

    @NotNull
    @Length(min = 1)
    private String key;

    private Long valueLong;
    private String valueString;

    public TestTemplateValue(TestTemplateParameter parameter, String key, Long valueLong) {
        this.key = key;
        this.valueLong = valueLong;
        this.templateParameter = parameter;
    }

    public TestTemplateValue(TestTemplateParameter parameter, String key, String valueString) {
        this.key = key;
        this.valueString = valueString;
        this.templateParameter = parameter;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSerializedValue() {
        if (valueLong != null) {
            return valueLong.toString();
        }
        return valueString;
    }

    public Long getValueLong() {
        return valueLong;
    }

    public void setValueLong(Long valueLong) {
        this.valueLong = valueLong;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public TestTemplateParameter getTemplateParameter() {
        return templateParameter;
    }

    public void setTemplateParameter(TestTemplateParameter templateParameter) {
        this.templateParameter = templateParameter;
    }

    @Override
    public String toString() {
        return "TestTemplateValue{" +
                "key='" + key + '\'' +
                ", valueLong=" + valueLong +
                ", valueString='" + valueString + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestTemplateValue)) return false;
        if (!super.equals(o)) return false;

        TestTemplateValue that = (TestTemplateValue) o;

        if (getTemplateParameter() != null ? !getTemplateParameter().equals(that.getTemplateParameter()) : that.getTemplateParameter() != null)
            return false;
        if (getKey() != null ? !getKey().equals(that.getKey()) : that.getKey() != null) return false;
        if (getValueLong() != null ? !getValueLong().equals(that.getValueLong()) : that.getValueLong() != null)
            return false;
        return getValueString() != null ? getValueString().equals(that.getValueString()) : that.getValueString() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getTemplateParameter() != null ? getTemplateParameter().hashCode() : 0);
        result = 31 * result + (getKey() != null ? getKey().hashCode() : 0);
        result = 31 * result + (getValueLong() != null ? getValueLong().hashCode() : 0);
        result = 31 * result + (getValueString() != null ? getValueString().hashCode() : 0);
        return result;
    }
}
