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

package testframework.testplatform.mapper;

import java.util.Collection;
import java.util.List;


//Source: https://github.com/mschvarc/PB138-Inventory-Management/blob/dfe413c1df56cae262c5d19342c8e66a5df1b225/backend/src/main/java/pb138/service/mapper/Automapper.java

/**
 * Interface for mapping between DAL Entities and DTOs in Web layer
 */
public interface Automapper {

    /**
     * Maps a generic collection of one Bean type onto targetClass Bean
     *
     * @param beanCollection input
     * @param targetClass    target type
     * @param <Bean>         output target type
     * @return mapped collection
     */
    <Bean> List<Bean> mapTo(Collection<?> beanCollection, Class<Bean> targetClass);

    /**
     * Maps one bean to another
     *
     * @param bean        input
     * @param targetClass target type
     * @param <Bean>      output target type
     * @return mapped bean
     */
    <Bean> Bean mapTo(Object bean, Class<Bean> targetClass);
}
