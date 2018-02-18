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

import org.springframework.stereotype.Component;
import testframework.testplatform.dal.exceptions.EntityValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

//Source: https://docs.jboss.org/hibernate/validator/6.0/reference/en-US/html_single

/**
 * Validates ORM entity constraints
 *
 * @author Martin Schvarcbacher
 */
@Component
public class ConstraintValidatorImpl implements ConstraintValidator {

    private final Validator validator;

    /**
     * Constructs a default validator
     */
    public ConstraintValidatorImpl() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
        if (this.validator == null) {
            throw new IllegalStateException("failed to get a validator");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void validate(T entity) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
        StringBuilder errors = new StringBuilder();
        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            errors.append(constraintViolation.getRootBean())
                    .append(" ")
                    .append(constraintViolation.getMessage())
                    .append(System.lineSeparator());
        }

        if (!constraintViolations.isEmpty()) {
            throw new EntityValidationException("Failed to validate: "
                    + entity.getClass()
                    + ", caused by: "
                    + errors.toString());
        }
        // NOTE: if X.Y is NULL, validator should NOT throw an exception,
        // if it does, check X.hashCode() implementation for null checks
        // https://hibernate.atlassian.net/browse/HV-1013
        // http://stackoverflow.com/questions/30333779/javax-validation-validationexception-hv000041-call-to-traversableresolver-isre

    }
}
