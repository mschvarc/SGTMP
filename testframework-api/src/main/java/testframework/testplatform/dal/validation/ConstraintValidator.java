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

package testframework.testplatform.dal.validation;

import testframework.testplatform.dal.exceptions.EntityValidationException;

/**
 * Validates ORM entity constraints
 *
 * @author Martin Schvarcbacher
 */
public interface ConstraintValidator {
    /**
     * Validates entity
     *
     * @param entity   input
     * @param <Entity> ORM entity type
     * @throws EntityValidationException on constraint violation
     */
    <Entity> void validate(Entity entity);
}
