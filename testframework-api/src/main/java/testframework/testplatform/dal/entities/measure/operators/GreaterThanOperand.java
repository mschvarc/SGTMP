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

package testframework.testplatform.dal.entities.measure.operators;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import testframework.testplatform.dal.entities.measure.Measure;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GreaterThanOperand.class, name = "GreaterThanOperand"),
})
@Entity
public class GreaterThanOperand extends ExpectedResult {


    @ManyToOne(cascade = CascadeType.ALL)
    private Measure left;

    public GreaterThanOperand() {
    }

    public GreaterThanOperand(Measure left) {
        this.left = left;
    }

    public Measure getLeft() {
        return left;
    }

    public void setLeft(Measure left) {
        this.left = left;
    }

    @Override
    public boolean isInRange(Measure measure) {
        return left.compareTo(measure) < 0;
    }

    @Override
    public String toString() {
        return "GreaterThanOperand{" +
                "left=" + left +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GreaterThanOperand)) return false;
        if (!super.equals(o)) return false;

        GreaterThanOperand that = (GreaterThanOperand) o;

        return getLeft() != null ? getLeft().equals(that.getLeft()) : that.getLeft() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getLeft() != null ? getLeft().hashCode() : 0);
        return result;
    }
}
