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

package testframework.testplatform.dal.exceptions;

/**
 * Represents exceptions thrown on constraint violations
 *
 * @author Martin Schvarcbacher
 */
public class EntityValidationException extends RuntimeException {

    /**
     * Represents exceptions thrown on constraint violations
     *
     * @param message message
     */
    public EntityValidationException(String message) {
        super(message);
    }

    /**
     * Represents exceptions thrown on constraint violations
     *
     * @param message message
     * @param cause   parent exception
     */
    public EntityValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
