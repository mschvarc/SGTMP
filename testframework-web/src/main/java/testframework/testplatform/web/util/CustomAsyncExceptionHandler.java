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

package testframework.testplatform.web.util;

import org.apache.log4j.Logger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

//Source: http://www.baeldung.com/spring-async
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static Logger logger = Logger.getLogger(AsyncUncaughtExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
        logger.error("Exception message - " + throwable.getMessage());
        logger.error("Method name - " + method.getName());
        for (Object param : obj) {
            logger.error("Parameter value - " + param);
        }
    }
}
