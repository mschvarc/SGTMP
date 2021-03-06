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
import testframework.testplatform.dal.entities.BaseEntity;
import testframework.testplatform.dal.entities.measure.Measure;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrOperand.class, name = "OrOperand"),
        @JsonSubTypes.Type(value = AndOperand.class, name = "AndOperand"),
        @JsonSubTypes.Type(value = EqualsOperand.class, name = "EqualsOperand"),
        @JsonSubTypes.Type(value = GreaterThanOperand.class, name = "GreaterThanOperand"),
        @JsonSubTypes.Type(value = LessThanOperand.class, name = "LessThanOperand"),
})


@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ExpectedResult extends BaseEntity {

    public abstract boolean isInRange(Measure measure);

}
