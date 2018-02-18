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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Measure extends BaseEntity implements Comparable<Measure> {

    private long value;
    @Enumerated
    private MeasureType measureType;

    @ManyToOne(cascade = CascadeType.ALL)
    private MeasureAttribute measureAttribute;

    private String unit;


    public Measure() {
    }

    public Measure(long value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public int compareTo(Measure other) {
        return Long.compare(value, other.value);

    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public MeasureType getMeasureType() {
        return measureType;
    }

    public void setMeasureType(MeasureType measureType) {
        this.measureType = measureType;
    }

    public MeasureAttribute getMeasureAttribute() {
        return measureAttribute;
    }

    public void setMeasureAttribute(MeasureAttribute measureAttribute) {
        this.measureAttribute = measureAttribute;
    }

    @Override
    public String toString() {
        return "Measure{" +
                "value=" + value +
                ", measureType=" + measureType +
                ", measureAttribute=" + measureAttribute +
                ", unit='" + unit + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Measure)) return false;
        if (!super.equals(o)) return false;

        Measure measure = (Measure) o;

        if (getValue() != measure.getValue()) return false;
        if (getMeasureType() != measure.getMeasureType()) return false;
        if (getMeasureAttribute() != null ? !getMeasureAttribute().equals(measure.getMeasureAttribute()) : measure.getMeasureAttribute() != null)
            return false;
        return getUnit() != null ? getUnit().equals(measure.getUnit()) : measure.getUnit() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (getValue() ^ (getValue() >>> 32));
        result = 31 * result + (getMeasureType() != null ? getMeasureType().hashCode() : 0);
        result = 31 * result + (getMeasureAttribute() != null ? getMeasureAttribute().hashCode() : 0);
        result = 31 * result + (getUnit() != null ? getUnit().hashCode() : 0);
        return result;
    }
}
